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

import static de.siegmar.jmonkey.encoder.script.parser.ScummParserHelper.json;
import static de.siegmar.jmonkey.encoder.script.parser.ScummParserHelper.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.siegmar.jmonkey.encoder.script.parser.statement.BinaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BooleanLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.Identifier;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;

class EqualityTest {

    @Test
    void equality() {
        final String program = "x + 5 == 10;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("==",
                    BinaryExpression.of("+",
                        Identifier.of("x"),
                        NumericLiteralExpression.of(5)
                    ),
                    NumericLiteralExpression.of(10)
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void notEqual() {
        final String program = "x + 5 != 10;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("!=",
                    BinaryExpression.of("+",
                        Identifier.of("x"),
                        NumericLiteralExpression.of(5)
                    ),
                    NumericLiteralExpression.of(10)
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void bool() {
        final String program = "x > 0 == true;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("==",
                    BinaryExpression.of(">",
                        Identifier.of("x"),
                        NumericLiteralExpression.of(0)
                    ),
                    BooleanLiteralExpression.of(true)
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

}
