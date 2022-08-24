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

package de.siegmar.jmonkey.encoder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.lang.Preconditions;

public final class Node {

    private final int offset;
    private final String name;
    private final Node parent;
    private final List<Node> children = new ArrayList<>();
    private Object data;
    private boolean dataContainsHeader;

    private Node(final Node parent, final String name, final int offset) {
        Preconditions.checkArgument(name.length() == 2);
        this.parent = parent;
        this.name = name;
        this.offset = offset;
    }

    public static Node createRoot(final String name) {
        Preconditions.checkArgument(name.length() == 2);
        return new Node(null, name, 0);
    }

    public static Node createRoot(final BasicChunk basicChunk) {
        return createRoot(basicChunk.header().name())
            .setData(basicChunk.data());
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    public Node setData(final BasicChunk basicChunk) {
        return setData(basicChunk.data());
    }

    public Node setData(final ByteString data) {
        this.data = data;
        this.dataContainsHeader = false;
        return this;
    }

    public Node setData(final Path file) {
        this.data = file;
        this.dataContainsHeader = false;
        return this;
    }

    public Node setDataWithHeader(final Path file) {
        this.data = file;
        this.dataContainsHeader = true;
        return this;
    }

    public Node createChild(final String childName) {
        final int nextOffset = size();
        final Node node = new Node(this, childName, nextOffset);
        children.add(node);
        return node;
    }

    public Node createChild(final BasicChunk basicChunk) {
        return createChild(basicChunk.header().name()).setData(basicChunk.data());
    }

    public int getOffset() {
        return offset;
    }

    public int size() {
        return sizeOfHeader() + sizeOfData() + getSizeOfChildren();
    }

    private int sizeOfHeader() {
        return dataContainsHeader ? 0 : 6;
    }

    private int sizeOfData() {
        if (data == null) {
            return 0;
        }

        if (data instanceof ByteString bs) {
            return bs.size();
        } else if (data instanceof Path p) {
            try {
                return Math.toIntExact(Files.size(p));
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        throw new IllegalStateException();
    }

    private int getSizeOfChildren() {
        return children.stream()
            .mapToInt(Node::size)
            .sum();
    }

    public void writeTo(final LeWriter out) throws IOException {
        writeHeader(out);
        writeData(out);
        writeChildren(out);
    }

    private void writeHeader(final LeWriter out) {
        if (!dataContainsHeader) {
            out.writeU32(size());
            out.write(name.getBytes(StandardCharsets.US_ASCII));
        }
    }

    private void writeData(final LeWriter out) throws IOException {
        if (data != null) {
            if (data instanceof ByteString bs) {
                bs.writeTo(out);
            } else if (data instanceof Path p) {
                Files.copy(p, out);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private void writeChildren(final LeWriter out) throws IOException {
        for (final Node child : children) {
            child.writeTo(out);
        }
    }

}
