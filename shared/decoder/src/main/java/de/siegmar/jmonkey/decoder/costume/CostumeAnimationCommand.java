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

package de.siegmar.jmonkey.decoder.costume;

import java.util.HexFormat;
import java.util.StringJoiner;

public class CostumeAnimationCommand {

    private final int command;

    CostumeAnimationCommand(final int command) {
        this.command = command;
    }

    public int getCommand() {
        return command;
    }

    public boolean isControl() {
        return isSound() || isStop() || isStart() || isHide();
    }

    public boolean isSound() {
        return command == 0x78;
    }

    public boolean isStop() {
        return command == 0x79;
    }

    public boolean isStart() {
        return command == 0x7a;
    }

    public boolean isHide() {
        return command == 0x7b;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CostumeAnimationCommand.class.getSimpleName() + "[", "]")
            .add("command=" + HexFormat.of().toHexDigits(command))
            .toString();
    }

}
