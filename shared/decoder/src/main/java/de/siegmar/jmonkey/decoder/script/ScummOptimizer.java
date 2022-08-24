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

package de.siegmar.jmonkey.decoder.script;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScummOptimizer {

    public static final Pattern POSITION_REGEX = Pattern.compile("\\[([0-9A-F]{4})]");
    public static final Pattern GOTO_REGEX = Pattern.compile("^(.* goto )(.{4})(.*)$");

    private ScummOptimizer() {
    }

    public static List<String> optimize(final List<String> script) {
        List<String> ret = labelGoto(script);

        ret = removeComments(ret);

        return ret;
    }

    public static List<String> labelGoto(final List<String> script) {
        final LabelGenerator labelGenerator = new LabelGenerator();

        final Map<String, LabelableStatement> positionedStatements = new LinkedHashMap<>();
        script.forEach(s -> positionedStatements.put(extractPosition(s).orElse(s), new LabelableStatement(s)));

        for (final LabelableStatement statement : positionedStatements.values()) {
            final Matcher matcher = GOTO_REGEX.matcher(statement.getStatement());
            if (matcher.find()) {
                final String jumpTarget = matcher.group(2);

                // Find & modify jump target
                final LabelableStatement stmt = positionedStatements.get(jumpTarget);
                if (stmt == null) {
                    throw new IllegalStateException("Could not find statement at pos " + jumpTarget);
                }
                final String label = stmt.addLabel(labelGenerator);

                // Modify goto
                statement.setStatement(matcher.replaceAll("$1" + label + "$3"));
            }
        }

        return positionedStatements.values().stream()
            .map(LabelableStatement::getStatement).toList();
    }

    private static Optional<String> extractPosition(final String statement) {
        final Matcher matcher = POSITION_REGEX.matcher(statement);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }

    private static List<String> removeComments(final List<String> ret) {
        return ret.stream()
            .map(s -> s.replaceFirst("^/\\*.+\\*/\s*", ""))
            .toList();
    }

    private static class LabelableStatement {

        private String statement;
        private String label;

        LabelableStatement(final String statement) {
            this.statement = statement;
        }

        String getStatement() {
            return statement;
        }

        void setStatement(final String statement) {
            this.statement = statement;
        }

        String addLabel(final LabelGenerator labelGenerator) {
            if (label == null) {
                label = labelGenerator.generate();
                statement = statement.replace("*/ ", "*/ " + label + ": ");
            }
            return label;
        }

    }

    private static class LabelGenerator {

        private int cnt;

        public String generate() {
            return "label" + cnt++;
        }

    }

}
