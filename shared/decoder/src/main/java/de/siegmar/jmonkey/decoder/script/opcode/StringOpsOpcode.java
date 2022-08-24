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
import de.siegmar.jmonkey.decoder.script.ScummString;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class StringOpsOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x27);

    public StringOpsOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int subOpcode = bb.readU8();
        switch (subOpcode & 0x1F) {
            case 1 -> putCodeInString(bb, subOpcode, opcodeDelegate);
            case 2 -> copyString(bb, subOpcode, opcodeDelegate);
            case 0x03 -> setStringChar(bb, subOpcode, opcodeDelegate);
            case 0x04 -> getStringChar(bb, subOpcode, opcodeDelegate);
            case 0x05 -> createString(bb, subOpcode, opcodeDelegate);
            default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
        }
    }

    private void putCodeInString(final EnhancedByteBuffer bb, final int subOpcode,
                                 final OpcodeDelegate opcodeDelegate) {
        final OpParameter stringId = resolveParameter8(bb, subOpcode, 0);
        final ScummString str = getString(bb);
        opcodeDelegate.putCodeInString(stringId, str);
    }

    private void copyString(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter destId = resolveParameter8(bb, subOpcode, 0);
        final OpParameter srcId = resolveParameter8(bb, subOpcode, 1);
        opcodeDelegate.copyString(destId, srcId);
    }

    private void setStringChar(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter stringId = resolveParameter8(bb, subOpcode, 0);
        final OpParameter index = resolveParameter8(bb, subOpcode, 1);
        final OpParameter ch = resolveParameter8(bb, subOpcode, 2);
        opcodeDelegate.setStringChar(stringId, index, ch);
    }

    private void getStringChar(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter result = resolvePointer(bb);
        final OpParameter stringId = resolveParameter8(bb, subOpcode, 0);
        final OpParameter index = resolveParameter8(bb, subOpcode, 1);
        opcodeDelegate.getStringChar(result, stringId, index);
    }

    private void createString(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter stringId = resolveParameter8(bb, subOpcode, 0);
        final OpParameter size = resolveParameter8(bb, subOpcode, 1);
        opcodeDelegate.createString(stringId, size);
    }

}
