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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.decoder.script.OpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class SetVarRangeOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x26, 0xA6);

    public SetVarRangeOpcode() {
        super(OPCODES);
    }

    // opcode result number[8] values[8]...
    // - or -
    // opcode result number[8] values[16]...
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final OpParameter result = resolvePointer(bb);
        final int number = bb.readU8();
        final List<Integer> varlist = parseList(bb, opcode, number);
        opcodeDelegate.setVarRange(result, number, varlist);
    }

    private List<Integer> parseList(final EnhancedByteBuffer bb, final int opcode, final int len) {
        return IntStream.range(0, len)
            .mapToObj(i -> (opcode & 0x80) != 0 ? bb.readU16() : bb.readU8())
            .collect(Collectors.toList());
    }

}
