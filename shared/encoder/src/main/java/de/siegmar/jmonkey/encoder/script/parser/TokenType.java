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

public enum TokenType {

    NUMBER,
    STRING,
    SEMICOLON,
    ADDITIVE_OPERATOR,
    MULTIPLICATIVE_OPERATOR,
    EQUALITY_OPERATOR,
    RELATIONAL_OPERATOR,
    IDENTIFIER,
    SIMPLE_ASSIGN,
    COMPLEX_ASSIGN,
    LPAREN,
    RPAREN,
    UNLESS,
    LOGICAL_NOT,
    DOT,
    LSPAREN,
    RSPAREN,
    COMMA,
    TRUE,
    FALSE,
    GOTO,
    INCREMENT,
    LABEL,
    EVAL,
    DECREMENT

}
