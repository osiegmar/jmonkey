/*
 * JMonkey - Java based development kit for "The Secret of Monkey Island"
 * Copyright (C) 2022  Oliver Siegmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.siegmar.jmonkey.decoder.script;

import static java.lang.System.Logger.Level.TRACE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.misc.MDC;
import de.siegmar.jmonkey.decoder.script.opcode.Opcode;

public class Script {

    private static final System.Logger LOG = System.getLogger(Script.class.getName());

    private final int scriptId;
    private final ScriptDecoder scriptDecoder;
    private final OpcodeDelegate opcodeDelegate;
    private final EnhancedByteBuffer bb;
    private final AtomicBoolean breaker = new AtomicBoolean(true);
    private final boolean freezeResistant;
    private final boolean recursive;
    private final int startPos;
    private int currentOpcodePos;
    private final Map<Integer, ScummInteger> localVariables = new HashMap<>();

    private ScriptStatus status = ScriptStatus.ACTIVE;
    private int frozen;
    private long delayedUntil;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public Script(final int scriptId, final ScriptDecoder scriptDecoder, final OpcodeDelegate opcodeDelegate,
                  final ByteString data, final int scriptPos, final List<Integer> args,
                  final boolean freezeResistant, final boolean recursive) {

        LOG.log(TRACE, "Initialize script (%s, %s, %s)",
            args, freezeResistant, recursive);

        this.scriptId = scriptId;
        this.scriptDecoder = scriptDecoder;
        this.opcodeDelegate = opcodeDelegate;
        bb = data.ebbLE();
        bb.position(scriptPos);
        final int argsSize = args.size();
        for (int i = 0; i < argsSize; i++) {
            setVariable(i, new ScummInteger(args.get(i)));
        }
        this.freezeResistant = freezeResistant;
        this.recursive = recursive;
        startPos = bb.position();
    }

    public int getScriptId() {
        return scriptId;
    }

    public void execute() {
        MDC.put("script", scriptId);

        if (delayedUntil > System.currentTimeMillis()) {
            LOG.log(TRACE, "delayed for %d msec",
                delayedUntil - System.currentTimeMillis());
            return;
        }

        LOG.log(TRACE, "execute()");

        while (status == ScriptStatus.ACTIVE && frozen == 0 && breaker.getAndSet(true) && bb.hasRemaining()) {
            currentOpcodePos = bb.position();
            final int opcode = bb.readU8();

            opcodeDelegate.setScript(this);
            opcodeDelegate.beforeOpcode(currentOpcodePos, opcode);

            opcodeDelegate.setScript(this);
            decoder(opcode)
                .execute(opcode, bb, opcodeDelegate);

            opcodeDelegate.setScript(this);
            opcodeDelegate.afterOpcode();
        }

        if (!bb.hasRemaining()) {
            if (recursive) {
                bb.position(startPos);
            } else {
                opcodeDelegate.endScript();

                status = ScriptStatus.STOPPED;
            }
        }

        MDC.remove("script");
    }

    // called from ExpressionOpcode TODO: refactor
    public Object callOpcode() {
        opcodeDelegate.setScript(this);
        opcodeDelegate.startExpression();

        final int opcode = bb.readU8();

        opcodeDelegate.setScript(this);
        decoder(opcode)
            .execute(opcode, bb, opcodeDelegate);

        opcodeDelegate.setScript(this);
        return opcodeDelegate.endExpression();
    }

    private Opcode decoder(final int opcode) {
        return scriptDecoder.getOpcodeDecoder(opcode)
            .orElseThrow(() -> new IllegalStateException("No impl for opcode %02X found".formatted(opcode)));
    }

    public void breakHere() {
        LOG.log(TRACE, "breakHere()");
        breaker.set(false);
    }

    public void redo() {
        gotoOffset(currentOpcodePos);
        breakHere();
    }

    public void gotoOffset(final int offset) {
        LOG.log(TRACE, "Jump to %04X", offset);
        bb.position(offset);
    }

    public void delay(final int duration) {
        LOG.log(TRACE, "delay(%d)", duration);
        delayedUntil = System.currentTimeMillis() + jiffiesToMillis(duration);
        breakHere();
    }

    private static long jiffiesToMillis(final int duration) {
        return duration * 1000L / 60;
    }

    public void stop() {
        LOG.log(TRACE, "stop()");
        status = ScriptStatus.STOPPED;
    }

    public boolean isFreezeResistant() {
        return freezeResistant;
    }

    public void freeze() {
        LOG.log(TRACE, "freeze()");
        frozen++;
    }

    public void unfreeze() {
        LOG.log(TRACE, "unfreeze()");
        if (frozen > 0) {
            frozen--;
        }
    }

    public boolean isStopped() {
        LOG.log(TRACE, "isStopped()");
        return status == ScriptStatus.STOPPED;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Script.class.getSimpleName() + "[", "]")
            .add("freezeResistant=" + freezeResistant)
            .add("recursive=" + recursive)
            .add("startPos=" + startPos)
            .add("args=" + localVariables)
            .toString();
    }

    public ScummInteger resolveVariable(final int address) {
        return localVariables.computeIfAbsent(address, a -> new ScummInteger(0));
    }

    public void setVariable(final int address, final ScummInteger assignVar) {
        localVariables.put(address, assignVar);
    }

}
