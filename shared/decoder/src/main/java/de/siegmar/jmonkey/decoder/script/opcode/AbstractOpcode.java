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

package de.siegmar.jmonkey.decoder.script.opcode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.ScummString;
import de.siegmar.jmonkey.decoder.script.ScummVars;
import de.siegmar.jmonkey.decoder.script.VarType;
import de.siegmar.jmonkey.decoder.script.parameter.ConstantParameter;
import de.siegmar.jmonkey.decoder.script.parameter.NamedVariable;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;
import de.siegmar.jmonkey.decoder.script.parameter.ParameterBuilder;

public abstract class AbstractOpcode implements Opcode {

    private static final Charset CHARSET = Charset.forName("CP850");

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final Set<Integer> opcodes;

    public AbstractOpcode(final Set<Integer> opcodes) {
        this.opcodes = opcodes;
    }

    // TODO rename
    @Override
    public Set<Integer> opcodes() {
        return opcodes;
    }

    @Override
    public synchronized void execute(final int opcode, final EnhancedByteBuffer bb,
                                     final OpcodeDelegate opcodeDelegate) {
        // FIXME no thread safety
        doDecode(opcode, bb, opcodeDelegate);
    }

    protected abstract void doDecode(int opcode, EnhancedByteBuffer bb, OpcodeDelegate opcodeDelegate);

    protected OpParameter resolveParameter8(final EnhancedByteBuffer bb, final int opcode, final int pos) {
        return isPointer(opcode, pos) ? resolvePointer(bb) : new ConstantParameter(bb.readU8());
    }

    public static OpParameter resolveParameter16(final EnhancedByteBuffer bb, final int opcode, final int pos) {
        return isPointer(opcode, pos) ? resolvePointer(bb) : new ConstantParameter(bb.readS16());
    }

    private static boolean isPointer(final int opcode, final int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 3);
        return (opcode & 0x80 >> pos) != 0;
    }

    public static OpParameter resolvePointer(final EnhancedByteBuffer bb) {
        final int word1 = bb.readU16();

        final var resolvedVar = ScummVars.resolve(word1);
        if (resolvedVar.isPresent()) {
            return new NamedVariable(resolvedVar.get());
        }

        final ParameterBuilder parameterBuilder = new ParameterBuilder(VarType.of(word1));
        parameterBuilder.appendPointer(word1 & 0xFFF);

        if ((word1 & 0x2000) != 0) {
            int word2 = bb.readU16();
            if ((word2 & 0x2000) != 0) {
                word2 ^= 0x2000;
                parameterBuilder.appendPointer(VarType.of(word2), word2 & 0xFFF);
            } else {
                parameterBuilder.appendPointer(word2 & 0xFFF);
            }
        }

        return parameterBuilder.build();
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    protected ScummString getString(final EnhancedByteBuffer bb) {
        final ScummString.ScummStringBuilder ssb = ScummString.builder();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int cmd;

        while ((cmd = bb.readU8()) != 0) {
            if (cmd == 0xFE || cmd == 0xFF) {
                if (bos.size() > 0) {
                    ssb.text(bos.toByteArray(), bos.toString(CHARSET));
                    bos.reset();
                }
                final int opcode = bb.readU8();

                // I have no idea what's the difference between 0xFE and 0xFF
                // Keep the information to later re-compile the same bytecodes

                if (cmd == 0xFE) {
                    switch (opcode) {
                        case 1 -> ssb.newline2();
                        case 8 -> ssb.verbNewline();
                        default -> throw new IllegalStateException("opcode not found: %X".formatted(opcode));
                    }
                } else {
                    switch (opcode) {
                        case 1 -> ssb.newline();
                        case 2 -> ssb.keepText();
                        case 3 -> ssb.sleep();
                        case 4 -> ssb.getInt(resolvePointer(bb));
                        case 5 -> ssb.getVerb(resolvePointer(bb));
                        case 6 -> ssb.getName(resolvePointer(bb));
                        case 7 -> ssb.getString(resolvePointer(bb));
                        default -> throw new IllegalStateException("opcode not found: %X".formatted(opcode));
                    }
                }
            } else {
                bos.write((byte) cmd);
            }
        }

        if (bos.size() > 0) {
            ssb.text(bos.toByteArray(), bos.toString(CHARSET));
        }

        return ssb.build();
    }

    protected List<OpParameter> readList16(final EnhancedByteBuffer bb) {
        final List<OpParameter> args = new ArrayList<>();

        int i;
        while ((i = bb.readU8()) != 0xFF) {
            args.add(resolveParameter16(bb, i, 0));
        }

        return args;
    }

}
