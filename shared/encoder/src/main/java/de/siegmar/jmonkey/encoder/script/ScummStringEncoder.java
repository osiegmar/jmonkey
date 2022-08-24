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

import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.ByteStringBuilder;

// TODO refactor this!
@SuppressWarnings({
    "checkstyle:ExecutableStatementCount",
    "checkstyle:JavaNCSS",
    "checkstyle:CyclomaticComplexity",
    "checkstyle:NestedIfDepth"
})
public final class ScummStringEncoder {

    private static final Charset CHARSET = Charset.forName("CP850");

    private ScummStringEncoder() {
    }

    public static ByteString encode(final String str, final boolean print) {
        final ByteStringBuilder bsb = new ByteStringBuilder();

        final StringBuilder sb = new StringBuilder();

        boolean in = false;

        final StringCharacterIterator it = new StringCharacterIterator(str);
        while (it.current() != CharacterIterator.DONE) {
            final char c = it.current();
            if (in) {
                if (c == '}') {
                    in = false;
                    final String fnc = sb.toString();
                    sb.setLength(0);

                    final int marker;
                    final int opcode;
                    ByteString ptr = null;

                    if ("newline".equals(fnc)) {
                        marker = print ? 0xFF : 0xFE;
                        opcode = 1;
                    } else if ("keepText".equals(fnc)) {
                        marker = 0xFF;
                        opcode = 2;
                    } else if ("sleep".equals(fnc)) {
                        marker = 0xFF;
                        opcode = 3;
                    } else if ("verbNewline".equals(fnc)) {
                        marker = 0xFE;
                        opcode = 8;
                    } else if (fnc.startsWith("int(")) {
                        marker = 0xFF;
                        opcode = 4;
                        ptr = PointerEncoder.resolvePointer(extractArg(fnc, '(', ')'));
                    } else if (fnc.startsWith("verb(")) {
                        marker = 0xFF;
                        opcode = 5;
                        ptr = PointerEncoder.resolvePointer(extractArg(fnc, '(', ')'));
                    } else if (fnc.startsWith("name(")) {
                        marker = 0xFF;
                        opcode = 6;
                        ptr = PointerEncoder.resolvePointer(extractArg(fnc, '(', ')'));
                    } else if (fnc.startsWith("string(")) {
                        marker = 0xFF;
                        opcode = 7;
                        ptr = PointerEncoder.resolvePointer(extractArg(fnc, '(', ')'));
                    } else {
                        throw new IllegalStateException("Unknown: " + fnc);
                    }

                    bsb.appendU8(marker);
                    bsb.appendU8(opcode);
                    if (ptr != null) {
                        bsb.append(ptr);
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '{') {
                    in = true;
                    bsb.append(sb.toString().getBytes(CHARSET));
                    sb.setLength(0);
                } else {
                    if (c == '\\') {
                        final String hexStr = "0" + it.next() + it.next() + it.next();
                        sb.appendCodePoint(Integer.decode(hexStr));
                    } else if (c == '™') {
                        sb.append('\u000F');
                    } else {
                        sb.append(c);
                    }
                }
            }

            it.next();
        }

        bsb.append(sb.toString().getBytes(CHARSET));

        bsb.appendU8(0);

        return bsb.build();
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
