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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.siegmar.jmonkey.encoder.script.parser.ScummParser;
import de.siegmar.jmonkey.encoder.script.parser.ScummTokenizer;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;
import de.siegmar.jmonkey.encoder.script.parser.statement.Statement;

final class ScummParserHelper {

    private static final ObjectWriter OW = new ObjectMapper()
        .addMixIn(Program.class, AbstractBaseTypeMixIn.class)
        .addMixIn(Statement.class, AbstractBaseTypeMixIn.class)
        .writer()
        .withDefaultPrettyPrinter();

    private ScummParserHelper() {
    }

    public static String json(final Program expected) {
        try {
            return OW.writeValueAsString(expected);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Program parse(final String program) {
        final ScummTokenizer scummTokenizer = new ScummTokenizer(program);
        final ScummParser scummParser = new ScummParser(scummTokenizer);
        return scummParser.parse();
    }

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public abstract class AbstractBaseTypeMixIn {

    }

}
