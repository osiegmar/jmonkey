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

package de.siegmar.jmonkey.commons.io;

public enum DataChunkType {

    /** AdLib sound. */
    AD(true),

    /** Amiga sound. */
    AM(true),

    /** Background image. */
    BM(true),

    /** Walk box. */
    BX(true),

    /** Color cycling. */
    CC(true),

    /** Costume. */
    CO(true),

    /** Entry script. */
    EN(true),

    /** Exit script. */
    EX(true),

    /** Info. */
    FO(true),

    /** Room header. */
    HD(true),

    /** Local Script Count. **/
    LC(true),

    /** Main header. **/
    LE(false),

    /** Header for rooms, costumes, global scripts and sounds. */
    LF(false),

    /** Local script. */
    LS(true),

    /** Number of sounds. */
    NL(true),

    /** Object code. */
    OC(true),

    /** Object image. */
    OI(true),

    /** VGA palette. */
    PA(true),

    /** Room. */
    RO(false),

    /** Roland sound. */
    ROL(true),

    /** Scale data. */
    SA(true),

    /** Global script. */
    SC(true),

    /** Unknown. */
    SL(true),

    /** Sound. */
    SO(false),

    /** EGA palette. */
    SP(true),

    /** PC-Speaker sound. */
    WA(true);

    private final boolean dataOnly;

    DataChunkType(final boolean dataOnly) {
        this.dataOnly = dataOnly;
    }

    public static DataChunkType of(final String name) {
        for (final DataChunkType value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new IllegalStateException("Unknown name: " + name);
    }

    public boolean isDataOnly() {
        return dataOnly;
    }

}
