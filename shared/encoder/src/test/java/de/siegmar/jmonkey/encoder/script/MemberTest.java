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

import de.siegmar.jmonkey.encoder.script.parser.statement.AssignmentExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.BinaryExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.Identifier;
import de.siegmar.jmonkey.encoder.script.parser.statement.MemberExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;

class MemberTest {

    @Test
    void simple() {
        final String program = "x.y;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                MemberExpression.of(false,
                    Identifier.of("x"),
                    Identifier.of("y")
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void assignment() {
        final String program = "x.y = 1;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                AssignmentExpression.of("=",
                    MemberExpression.of(false,
                        Identifier.of("x"),
                        Identifier.of("y")
                    ),
                    NumericLiteralExpression.of(1)
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void assignmentComputed() {
        final String program = "x[0] = 1;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                AssignmentExpression.of("=",
                    MemberExpression.of(true,
                        Identifier.of("x"),
                        NumericLiteralExpression.of(0)
                    ),
                    NumericLiteralExpression.of(1)
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void assignmentRecursiveComputedSimple() {
        final String program = "x[1 + 2] = 3;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                AssignmentExpression.of("=",
                    MemberExpression.of(true,
                        Identifier.of("x"),
                        BinaryExpression.of("+",
                            NumericLiteralExpression.of(1),
                            NumericLiteralExpression.of(2)
                        )
                    ),
                    NumericLiteralExpression.of(3)
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void assignmentRecursiveComputed() {
        final String program = "x[y[0] + 5] = 1;";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                AssignmentExpression.of("=",
                    MemberExpression.of(true,
                        Identifier.of("x"),
                        BinaryExpression.of("+",
                            MemberExpression.of(true,
                                Identifier.of("y"),
                                NumericLiteralExpression.of(0)),
                            NumericLiteralExpression.of(5)
                        )
                    ),
                    NumericLiteralExpression.of(1)
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

}
