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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import de.siegmar.jmonkey.commons.lang.Preconditions;

public class EnhancedByteBuffer {

    private final ByteBuffer bb;

    public EnhancedByteBuffer(final ByteBuffer bb) {
        Preconditions.checkArgument(bb.isReadOnly());
        this.bb = bb;
    }

    public EnhancedByteBuffer bigEndian() {
        return new EnhancedByteBuffer(bb.order(ByteOrder.BIG_ENDIAN));
    }

    public boolean hasRemaining() {
        return bb.hasRemaining();
    }

    public int remaining() {
        return bb.remaining();
    }

    public int limit() {
        return bb.limit();
    }

    public int position() {
        return bb.position();
    }

    public EnhancedByteBuffer position(final int pos) {
        bb.position(pos);
        return this;
    }

    public EnhancedByteBuffer skip(final int i) {
        bb.position(bb.position() + i);
        return this;
    }

    public byte readS8() {
        return bb.get();
    }

    public int readU8() {
        return bb.get() & 0xff;
    }

    public int readU8(final int position) {
        return bb.get(position) & 0xff;
    }

    public int readS16() {
        return bb.getShort();
    }

    public int readU16() {
        return bb.getShort() & 0xffff;
    }

    public int readU16(final int position) {
        return bb.getShort(position) & 0xffff;
    }

    public int readS32() {
        return bb.getInt();
    }

    public int readU32() {
        final int read = bb.getInt();
        if (read < 0) {
            throw new IllegalStateException("read a signed integer: " + read);
        }
        return read;
    }

    public int readU32(final int position) {
        final int read = bb.getInt(position);
        if (read < 0) {
            throw new IllegalStateException("read a signed integer: " + read);
        }
        return read;
    }

    public String readNTS(final Charset charset) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int d;
        while ((d = bb.get()) != 0) {
            bos.write(d);
        }

        return bos.toString(charset);
    }

    public String readAscii(final int len) {
        final byte[] data = new byte[len];
        bb.get(data);
        return new String(data, StandardCharsets.US_ASCII);
    }

    public String readAscii(final int pos, final int len) {
        final byte[] data = new byte[len];
        bb.get(pos, data);
        return new String(data, StandardCharsets.US_ASCII);
    }

    public <T> List<T> readList(final int num, final Supplier<T> o) {
        return IntStream.range(0, num)
            .mapToObj(i -> o.get())
            .toList();
    }

    /*
     * @deprecated use {@link #readImmutableBytes()} instead
     */
    @Deprecated
    public byte[] readBytes(final int len) {
        final byte[] data = new byte[len];
        bb.get(data);
        return data;
    }

    public ByteString readImmutableBytes() {
        return readImmutableBytes(remaining());
    }

    public ByteString readImmutableBytes(final int len) {
        final ByteString bs = new ByteString(bb.slice(bb.position(), len));
        bb.position(bb.position() + len);
        return bs;
    }

    public BasicChunkHeader readChunkHeader() {
        final int chunkSize = readU32();
        final String chunkName = readAscii(2);
        return new BasicChunkHeader(chunkSize, chunkName);
    }

    public BasicChunk readChunk() {
        bb.mark();
        final BasicChunkHeader chunkHeader = readChunkHeader();
        bb.reset();
        final ByteString data = readImmutableBytes(chunkHeader.length());

        return new BasicChunk(chunkHeader, data);
    }

    public EnhancedByteBuffer slice() {
        return new EnhancedByteBuffer(bb.slice().order(ByteOrder.LITTLE_ENDIAN));
    }

    public EnhancedByteBuffer slice(final int index, final int length) {
        return new EnhancedByteBuffer(bb.slice(index, length).order(ByteOrder.LITTLE_ENDIAN));
    }

    public void save(final Path file) {
        try (SeekableByteChannel out = Files.newByteChannel(file, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            while (bb.hasRemaining()) {
                out.write(bb);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void copyTo(final ByteBuffer out) {
        out.put(bb);
    }

    public BitBuffer bitstream() {
        return new BitBuffer(bb, bb.order());
    }

}
