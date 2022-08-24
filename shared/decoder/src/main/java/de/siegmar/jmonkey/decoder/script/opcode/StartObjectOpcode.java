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

import java.util.List;
import java.util.Set;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class StartObjectOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x37, 0x77, 0xB7, 0xF7);

    public StartObjectOpcode() {
        super(OPCODES);
    }

    // opcode object[p16] script[p8] args[v16]...
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter obj = resolveParameter16(bb, opcode, 0);
        final OpParameter scriptId = resolveParameter8(bb, opcode, 1);
        final List<OpParameter> args = readList16(bb);
        opcodeDelegate.startObject(obj, scriptId, args);
    }

}
