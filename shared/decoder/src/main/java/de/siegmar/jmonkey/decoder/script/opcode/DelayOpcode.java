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

public class DelayOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x2E);

    public DelayOpcode() {
        super(OPCODES);
    }

    // opcode param[24]
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int delay = decodeDelay(bb);
        opcodeDelegate.delay(delay);
    }

    private int decodeDelay(final EnhancedByteBuffer bb) {
        int d;
        d = bb.readU8();
        d |= bb.readU8() << 8;
        d |= bb.readU8() << 16;
        return d;
    }

}
