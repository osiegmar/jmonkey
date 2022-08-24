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

package de.siegmar.jmonkey.encoder.script;

import java.util.Locale;
import java.util.Optional;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.ByteStringBuilder;
import de.siegmar.jmonkey.decoder.script.ScummVars;
import de.siegmar.jmonkey.decoder.script.VarType;

public final class PointerEncoder {

    private PointerEncoder() {
    }

    public static ByteString resolvePointer(final String arg) {
        final ByteStringBuilder innerBsb = new ByteStringBuilder();

        final Optional<Integer> scummVar = ScummVars.resolve(arg);
        if (scummVar.isPresent()) {
            innerBsb.appendU16(scummVar.get());
        } else {
            final VarType varType = VarType.valueOf(arg.substring(0, arg.indexOf('[')).toUpperCase(Locale.ROOT));
            final int nr = Integer.parseInt(extractArg(arg, '[', ']'));

            innerBsb.appendU16(varType.to(nr));
        }

        return innerBsb.build();
    }

    private static String extractArg(final String fnc, final char open, final char close) {
        int begin = -1;
        int end = -1;
        for (int i = 0; i < fnc.length(); i++) {
            final char c = fnc.charAt(i);
            if (begin < 0) {
                if (c == open) {
                    begin = i + 1;
                }
            } else {
                if (c == close) {
                    end = i;
                    break;
                }
            }
        }

        if (begin < 0 || end < 0) {
            throw new IllegalStateException("Invalid function call: " + fnc);
        }

        return fnc.substring(begin, end);
    }

}
