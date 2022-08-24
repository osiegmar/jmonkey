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
import de.siegmar.jmonkey.decoder.script.ResourceManager;
import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class ResourceRoutinesOpcode extends AbstractOpcode {

    private static final Set<Integer> OPCODES = Set.of(0x0C, 0x8C);

    public ResourceRoutinesOpcode() {
        super(OPCODES);
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public void doDecode(final int opcode, final EnhancedByteBuffer bb, final OpcodeDelegate opcodeDelegate) {
        final int subCode = bb.readU8();

        if ((subCode & 0x3F) != (subCode & 0x1F)) {
            throw new IllegalStateException("opcode not found: %X".formatted(subCode));
        }

        final ResourceManager resource = opcodeDelegate.resource();

        if (subCode == 17) {
            resource.clearHeap();
            return;
        }

        final OpParameter resId = resolveParameter8(bb, subCode, 0);

        switch (subCode & 0x3F) {
            case 1 -> resource.loadScript(resId);
            case 2 -> resource.loadSound(resId);
            case 3 -> resource.loadCostume(resId);
            case 4 -> resource.loadRoom(resId);
            case 7 -> resource.nukeCostume(resId);
            case 8 -> resource.nukeRoom(resId);
            case 9 -> resource.lockScript(resId);
            case 10 -> resource.lockSound(resId);
            case 11 -> resource.lockCostume(resId);
            case 12 -> resource.lockRoom(resId);
            case 13 -> resource.unlockScript(resId);
            case 14 -> resource.unlockSound(resId);
            case 15 -> resource.unlockCostume(resId);
            case 16 -> resource.unlockRoom(resId);
            case 18 -> resource.loadCharset(resId);
            default -> throw new IllegalStateException("Unknown subCode: %02X".formatted(subCode));
        }
    }

}
