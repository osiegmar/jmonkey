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
import de.siegmar.jmonkey.decoder.script.InputMode;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;

public class CursorCommandOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x2C);

    public CursorCommandOpcode() {
        super(OPCODES);
    }

    // opcode sub-opcode
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int subOpcode = bb.readU8();
        switch (subOpcode) {
            case 0x01 -> opcodeDelegate.cursor(InputMode.ON);
            case 0x02 -> opcodeDelegate.cursor(InputMode.OFF);
            case 0x03 -> opcodeDelegate.userput(InputMode.ON);
            case 0x04 -> opcodeDelegate.userput(InputMode.OFF);
            case 0x05 -> opcodeDelegate.cursor(InputMode.SOFT_ON);
            case 0x06 -> opcodeDelegate.cursor(InputMode.SOFT_OFF);
            case 0x07 -> opcodeDelegate.userput(InputMode.SOFT_ON);
            case 0x08 -> opcodeDelegate.userput(InputMode.SOFT_OFF);
            case 0x0D -> opcodeDelegate.initCharset(resolveParameter8(bb, subOpcode, 0));
            default -> throw new IllegalStateException("opcode not found: %X".formatted(subOpcode));
        }
    }

}
