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

public final class LabeledStatement extends Statement {

    private final Identifier label;
    private final Statement statement;

    private LabeledStatement(final Identifier label, final Statement statement) {
        this.label = label;
        this.statement = statement;
    }

    public static Statement of(final Identifier label, final Statement expression) {
        return new LabeledStatement(label, expression);
    }

    public Identifier getLabel() {
        return label;
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LabeledStatement that = (LabeledStatement) o;
        return Objects.equals(label, that.label) && Objects.equals(statement, that.statement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, statement);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LabeledStatement.class.getSimpleName() + "[", "]")
            .add("label=" + label)
            .add("statement=" + statement)
            .toString();
    }

}
