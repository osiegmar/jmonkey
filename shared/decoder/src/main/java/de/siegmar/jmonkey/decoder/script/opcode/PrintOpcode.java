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

import java.util.Set;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.PrintOps;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class PrintOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x14, 0x94, 0xD8);

    public PrintOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final PrintOps printOps;
        if (opcode == 0xD8) {
            printOps = opcodeDelegate.printEgo();
        } else {
            final OpParameter ego = resolveParameter8(bb, opcode, 0);
            printOps = opcodeDelegate.print(ego);
        }

        int subOpcode;
        LOOP:
        while ((subOpcode = bb.readU8()) != 0xFF) {
            switch (subOpcode & 0xF) {
                case 0 -> printOps.pos(
                    resolveParameter16(bb, subOpcode, 0),
                    resolveParameter16(bb, subOpcode, 1));
                case 1 -> printOps.color(resolveParameter8(bb, subOpcode, 0));
                case 2 -> printOps.clipped(resolveParameter16(bb, subOpcode, 0));
                case 4 -> printOps.center();
                case 6 -> printOps.left();
                case 7 -> printOps.overhead();
                case 15 -> {
                    printOps.text(getString(bb));
                    break LOOP;
                }
                default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
            }
        }

        printOps.end();
    }

}
