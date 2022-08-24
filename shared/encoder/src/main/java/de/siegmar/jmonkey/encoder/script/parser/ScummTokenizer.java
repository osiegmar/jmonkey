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

package de.siegmar.jmonkey.encoder.script.parser;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScummTokenizer implements Iterator<Token> {

    private static final Map<Pattern, TokenType> SPEC;

    private final String string;
    private int pos;

    static {
        final Map<Pattern, TokenType> spec = new LinkedHashMap<>();

        // Whitespace
        spec.put(Pattern.compile("^\\s+"), null);

        // Single line comments
        spec.put(Pattern.compile("^//.*"), null);

        // Multi line comments
        spec.put(Pattern.compile("^/\\*[\\s\\S]*?\\*/"), null);

        // Symbols and Delimiter
        spec.put(Pattern.compile("^;"), TokenType.SEMICOLON);
        spec.put(Pattern.compile("^\\("), TokenType.LPAREN);
        spec.put(Pattern.compile("^\\)"), TokenType.RPAREN);
        spec.put(Pattern.compile("^\\."), TokenType.DOT);
        spec.put(Pattern.compile("^,"), TokenType.COMMA);
        spec.put(Pattern.compile("^\\["), TokenType.LSPAREN);
        spec.put(Pattern.compile("^]"), TokenType.RSPAREN);
        spec.put(Pattern.compile("^`"), TokenType.EVAL);

        // Keywords
        spec.put(Pattern.compile("^\\bunless\\b"), TokenType.UNLESS);
        spec.put(Pattern.compile("^\\bgoto\\b"), TokenType.GOTO);
        spec.put(Pattern.compile("^\\btrue\\b"), TokenType.TRUE);
        spec.put(Pattern.compile("^\\bfalse\\b"), TokenType.FALSE);

        // Numbers
        spec.put(Pattern.compile("^\\d+"), TokenType.NUMBER);

        // Identifiers
        spec.put(Pattern.compile("^\\w+:"), TokenType.LABEL);
        spec.put(Pattern.compile("^\\w+"), TokenType.IDENTIFIER);

        // Equality operators
        spec.put(Pattern.compile("^[=!]="), TokenType.EQUALITY_OPERATOR);

        // Increment / Decrement operators
        spec.put(Pattern.compile("^\\+\\+"), TokenType.INCREMENT);
        spec.put(Pattern.compile("^--"), TokenType.DECREMENT);

        // Assignment operators
        spec.put(Pattern.compile("^="), TokenType.SIMPLE_ASSIGN);
        spec.put(Pattern.compile("^[*/+-]="), TokenType.COMPLEX_ASSIGN);

        // Math operators
        spec.put(Pattern.compile("^[+-]"), TokenType.ADDITIVE_OPERATOR);
        spec.put(Pattern.compile("^[*/]"), TokenType.MULTIPLICATIVE_OPERATOR);

        // Relational operators
        spec.put(Pattern.compile("^[><]=?"), TokenType.RELATIONAL_OPERATOR);

        // Logical operators
        spec.put(Pattern.compile("^!"), TokenType.LOGICAL_NOT);

        spec.put(Pattern.compile("^\"[^\"]*\""), TokenType.STRING);
        SPEC = Collections.unmodifiableMap(spec);
    }

    public ScummTokenizer(final String string) {
        this.string = string;
    }

    @Override
    public boolean hasNext() {
        return pos < string.length();
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            return null;
        }

        final String remaining = string.substring(pos);

        for (final Map.Entry<Pattern, TokenType> spec : SPEC.entrySet()) {
            final String match = match(spec.getKey(), remaining);

            if (match != null) {
                if (spec.getValue() == null) {
                    return next();
                }
                return new Token(spec.getValue(), match);
            }
        }

        throw new IllegalStateException("Unexpected token: '" + remaining.charAt(0) + "'");
    }

    private String match(final Pattern pattern, final String str) {
        final Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            return null;
        }
        final String matched = matcher.group();
        pos += matched.length();
        return matched;
    }

}
