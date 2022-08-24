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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.decoder.LecFileScriptPrintDecoder;
import de.siegmar.jmonkey.decoder.script.PrintOpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.ScriptDecoder;
import de.siegmar.jmonkey.decoder.script.ScummOptimizer;
import de.siegmar.jmonkey.encoder.script.parser.ScummParser;
import de.siegmar.jmonkey.encoder.script.parser.ScummTokenizer;
import de.siegmar.jmonkey.encoder.script.parser.statement.Program;

@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
class ScummCompilerTest {

    private final ScummCompiler c = new ScummCompiler();

    @ParameterizedTest
    @MethodSource("singleStatements")
    void single(final String statement) {
        final Program parsedProgram = parse(statement);
        final ByteString compiledProgram = c.compile(parsedProgram);
        final Program decompiledProgram = parse(decode(compiledProgram));

        assertEquals(json(decompiledProgram), json(parsedProgram));
    }

    @ParameterizedTest
    @MethodSource("fullProgram")
    void full(final String program) {
        final Program parsedProgram = parse(program);
        final ByteString compiledProgram = c.compile(parsedProgram);
        final Program decompiledProgram = parse(decode(compiledProgram));

        assertEquals(json(decompiledProgram), json(parsedProgram));
    }

    private static String decode(final ByteString code) {
        final StringWriter sw = new StringWriter();

        final List<String> statements = new ArrayList<>();
        final PrintOpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(statements::add);
        final ScriptDecoder scriptDecoder = new ScriptDecoder();
        final LecFileScriptPrintDecoder lfspd = new LecFileScriptPrintDecoder(sw, scriptDecoder, opcodeDelegate);
        lfspd.decodeGeneric(code).execute();

        for (final String statement : ScummOptimizer.optimize(statements)) {
            sw.append(statement).append('\n');
        }

        return sw.toString();
    }

    private static Program parse(final String script) {
        return new ScummParser(new ScummTokenizer(script)).parse();
    }

    public static List<Arguments> fullProgram() {
        final StringBuilder sb = new StringBuilder();

        try (BufferedReader br = newBufferedReader("/full.scu")) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("//")) {
                    sb.append(line).append('\n');
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return List.of(Arguments.of(sb.toString()));
    }

    public static List<Arguments> singleStatements() {
        final List<Arguments> statements = new ArrayList<>();
        try (BufferedReader br = newBufferedReader("/single.scu")) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("//")) {
                    statements.add(Arguments.of(line));
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return statements;
    }

    private static BufferedReader newBufferedReader(final String name) {
        return new BufferedReader(new InputStreamReader(getResource(name), StandardCharsets.UTF_8));
    }

    private static InputStream getResource(final String name) {
        return ScummCompilerTest.class.getResourceAsStream(name);
    }

}
