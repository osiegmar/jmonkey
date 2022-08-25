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

import de.siegmar.jmonkey.encoder.script.parser.statement.AssignmentExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.CallExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.ExpressionStatement;
import de.siegmar.jmonkey.encoder.script.parser.statement.Identifier;
import de.siegmar.jmonkey.encoder.script.parser.statement.MemberExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.NumericLiteralExpression;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;

class CallTest {

    @Test
    void simple() {
        final String program = "foo();";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                CallExpression.of(
                    Identifier.of("foo"),
                    null
                )
            )));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void memberCall() {
        final String program = "foo.bar(x, y);";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                CallExpression.of(
                    MemberExpression.of(
                        false,
                        Identifier.of("foo"),
                        Identifier.of("bar")
                    ),
                    List.of(Identifier.of("x"), Identifier.of("y"))
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

    @Test
    void namedArguments() {
        final String program = "bar(x = 1, y = 2);";
        final Program expected = Program.of(List.of(
            ExpressionStatement.of(
                CallExpression.of(
                    Identifier.of("bar"),
                    List.of(
                        AssignmentExpression.of("=",
                            Identifier.of("x"),
                            NumericLiteralExpression.of(1)
                        ),
                        AssignmentExpression.of("=",
                            Identifier.of("y"),
                            NumericLiteralExpression.of(2)
                        ))
                ))));

        assertEquals(json(expected), json(parse(program)));
    }

}
