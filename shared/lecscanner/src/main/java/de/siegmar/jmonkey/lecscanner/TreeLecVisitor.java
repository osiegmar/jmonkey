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

package de.siegmar.jmonkey.lecscanner;

public class TreeLecVisitor implements LecVisitor {

    private TreeIndex<LecChunk> rootNode;
    private TreeIndex<LecChunk> lfNode;
    private TreeIndex<LecChunk> roomNode;
    private TreeIndex<LecChunk> soundNode;

    @Override
    public boolean visit(final LecFile lecFile, final LecChunk chunk) {
        final var node = new TreeIndex<>(chunk);

        final var container = switch (chunk.type()) {
            case LE -> {
                rootNode = node;
                yield null;
            }
            case LF -> {
                lfNode = node;
                yield rootNode;
            }
            case FO -> rootNode;
            case RO -> {
                roomNode = node;
                yield lfNode;
            }
            case SO -> {
                soundNode = node;
                yield lfNode;
            }
            case SC, CO, ROL, AM -> lfNode;
            case AD, WA -> soundNode;
            case BM, BX, CC, EN, EX, HD, LC, LS, NL, OC, OI, PA, SA, SL, SP -> roomNode;
        };

        if (container != null) {
            container.addChild(node);
        }

        return true;
    }

    public TreeIndex<LecChunk> getTree() {
        return rootNode;
    }

}
