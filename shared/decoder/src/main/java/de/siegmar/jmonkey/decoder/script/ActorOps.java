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

import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public interface ActorOps {

    void costume(OpParameter costume);

    void walkSpeed(OpParameter speedX, OpParameter speedY);

    void sound(OpParameter sound);

    void walkAnimNr(OpParameter anim);

    void talkAnimNr(OpParameter startFrame, OpParameter stopFrame);

    void standAnimNr(OpParameter anim);

    void init();

    void elevation(OpParameter elevation);

    void width(OpParameter width);

    void scale(OpParameter scale);

    void palette(OpParameter idx, OpParameter val);

    void talkColor(OpParameter color);

    void name(ScummString name);

    void initAnimNr(OpParameter anim);

    void end();

}
