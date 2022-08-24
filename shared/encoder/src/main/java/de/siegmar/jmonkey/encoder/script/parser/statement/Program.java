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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class Program {

    private final List<Statement> body;

    private Program(final List<Statement> body) {
        this.body = body;
    }

    public static Program of(final List<Statement> body) {
        return new Program(body);
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Program program = (Program) o;
        return Objects.equals(body, program.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Program.class.getSimpleName() + "[", "]")
            .add("body=" + body)
            .toString();
    }

}
