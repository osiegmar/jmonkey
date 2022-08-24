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

package de.siegmar.jmonkey.cli;

import static picocli.CommandLine.ScopeType.INHERIT;

import de.siegmar.jmonkey.cli.builder.BuildCommand;
import de.siegmar.jmonkey.cli.decrypt.DecryptCommand;
import de.siegmar.jmonkey.cli.export.ExportCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "monkey-cli",
    headerHeading = "@|bold,magenta JMonkey CLI – Command line utility for The Secret of Monkey Island|@%n%n",
    footer = "Copyright (C) 2022  Oliver Siegmar",
    version = "1.0-SNAPSHOT",
    mixinStandardHelpOptions = true,
    separator = " ",
    subcommands = {
        ExportCommand.class,
        BuildCommand.class,
        DecryptCommand.class
    },
    scope = INHERIT)
@SuppressWarnings({"checkstyle:UncommentedMain", "checkstyle:HideUtilityClassConstructor"})
public class Cli {

    public static void main(final String[] args) {
        System.exit(new CommandLine(new Cli()).execute(args));
    }

}
