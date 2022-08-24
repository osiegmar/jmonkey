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

public final class MemberExpression extends Expression {

    private final boolean computed;
    private final Expression object;
    private final Expression property;

    private MemberExpression(final boolean computed, final Expression object, final Expression property) {
        this.computed = computed;
        this.object = object;
        this.property = property;
    }

    public static MemberExpression of(final boolean computed, final Expression object, final Expression property) {
        return new MemberExpression(computed, object, property);
    }

    public boolean isComputed() {
        return computed;
    }

    public Expression getObject() {
        return object;
    }

    public Expression getProperty() {
        return property;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MemberExpression that = (MemberExpression) o;
        return computed == that.computed
            && Objects.equals(object, that.object)
            && Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(computed, object, property);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MemberExpression.class.getSimpleName() + "[", "]")
            .add("computed=" + computed)
            .add("object=" + object)
            .add("property=" + property)
            .toString();
    }

}
