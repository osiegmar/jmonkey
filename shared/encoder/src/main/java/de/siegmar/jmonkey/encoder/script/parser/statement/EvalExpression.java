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

package de.siegmar.jmonkey.encoder.script.parser.statement;

import java.util.Objects;
import java.util.StringJoiner;

public final class EvalExpression extends Expression {

    private final Expression expression;

    private EvalExpression(final Expression expression) {
        this.expression = expression;
    }

    public static EvalExpression of(final Expression expression) {
        return new EvalExpression(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EvalExpression that = (EvalExpression) o;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EvalExpression.class.getSimpleName() + "[", "]")
            .add("expression=" + expression)
            .toString();
    }

}
