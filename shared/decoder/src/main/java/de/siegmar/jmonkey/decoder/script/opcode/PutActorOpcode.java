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

public class PutActorOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x01, 0x21, 0x41, 0x61, 0x81, 0xA1, 0xC1, 0xE1);

    public PutActorOpcode() {
        super(OPCODES);
    }

    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter actor = resolveParameter8(bb, opcode, 0);
        final OpParameter x = resolveParameter16(bb, opcode, 1);
        final OpParameter y = resolveParameter16(bb, opcode, 2);
        opcodeDelegate.putActor(actor, x, y);
    }

}
