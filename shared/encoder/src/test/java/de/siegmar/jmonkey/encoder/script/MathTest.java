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

import static de.siegmar.jmonkey.encoder.script.ScummParserHelper.json;
import static de.siegmar.jmonkey.encoder.script.ScummParserHelper.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.siegmar.jmonkey.encoder.script.parser.statement.BinaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;

class MathTest {

    @Test
    void binaryExpression() {
        final String program = "2 + 3;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("+",
                    NumericLiteralExpression.of(2),
                    NumericLiteralExpression.of(3)
                )
            )));
        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void multipleBinaryExpression() {
        final String program = "2 + 3 - 1;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("-",
                    BinaryExpression.of("+",
                        NumericLiteralExpression.of(2),
                        NumericLiteralExpression.of(3)
                    ),
                    NumericLiteralExpression.of(1)
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void multiplicativeBinaryExpression() {
        final String program = "2 + 3 * 4;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("+",
                    NumericLiteralExpression.of(2),
                    BinaryExpression.of("*",
                        NumericLiteralExpression.of(3),
                        NumericLiteralExpression.of(4)
                    )
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void parenMultiplicativeBinaryExpression() {
        final String program = "(2 + 3) * 4;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                BinaryExpression.of("*",
                    BinaryExpression.of("+",
                        NumericLiteralExpression.of(2),
                        NumericLiteralExpression.of(3)
                    ),
                    NumericLiteralExpression.of(4)
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

}
