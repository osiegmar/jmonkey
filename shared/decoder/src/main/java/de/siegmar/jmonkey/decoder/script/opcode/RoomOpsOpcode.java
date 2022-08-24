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
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class RoomOpsOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x33, 0x73, 0xB3, 0xF3);

    public RoomOpsOpcode() {
        super(OPCODES);
    }

    // opcode 0x0A
    // opcode script[p8] args[v16]...
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int subOpcode = bb.readU8();
        switch (subOpcode & 0x1F) {
            case 0x01 -> roomScroll(bb, subOpcode, opcodeDelegate);
            case 0x02 -> roomColor(bb, subOpcode, opcodeDelegate);
            case 0x03 -> setScreen(bb, subOpcode, opcodeDelegate);
            case 0x04 -> setPalColor(bb, subOpcode, opcodeDelegate);
            case 0x05 -> opcodeDelegate.shake(true);
            case 0x06 -> opcodeDelegate.shake(false);
            default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
        }
    }

    private void roomColor(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter color = resolveParameter16(bb, subOpcode, 0);
        final OpParameter palIndex = resolveParameter16(bb, subOpcode, 1);
        opcodeDelegate.roomColor(color, palIndex);
    }

    private void roomScroll(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter minX = resolveParameter16(bb, subOpcode, 0);
        final OpParameter maxX = resolveParameter16(bb, subOpcode, 1);
        opcodeDelegate.roomScroll(minX, maxX);
    }

    private void setScreen(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter width = resolveParameter16(bb, subOpcode, 0);
        final OpParameter height = resolveParameter16(bb, subOpcode, 1);
        opcodeDelegate.setScreen(width, height);
    }

    private void setPalColor(final EnhancedByteBuffer bb, final int subOpcode, final OpcodeDelegate opcodeDelegate) {
        final OpParameter color = resolveParameter16(bb, subOpcode, 0);
        final OpParameter palIndex = resolveParameter16(bb, subOpcode, 1);
        opcodeDelegate.setPalColor(color, palIndex);
    }

}
