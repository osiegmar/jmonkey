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

package de.siegmar.jmonkey.datarepository;

import static java.lang.System.Logger.Level.DEBUG;
import static java.nio.file.StandardOpenOption.READ;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.BasicChunkHeader;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.io.IOUtil;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.decoder.header.ChunkFODecoder;

public class LecSeekableFile implements Closeable {

    private static final System.Logger LOG = System.getLogger(LecSeekableFile.class.getName());

    private final FileChannel file;
    private final int fileSize;
    private final LecIndex lecIndex;

    public LecSeekableFile(final Path file) throws IOException {
        LOG.log(DEBUG, () -> "Read LEC index from %s (SHA-1 hash: %s)"
            .formatted(file, IOUtil.toSHA1(file)));

        this.file = FileChannel.open(file, READ);
        fileSize = Math.toIntExact(this.file.size());
        lecIndex = readLecIndex();
    }

    private LecIndex readLecIndex() {
        final EnhancedByteBuffer bb = read(0, 512).ebbLE();

        // LE (file) header
        final BasicChunkHeader leHeader = bb.readChunkHeader();
        Assert.assertEqual(leHeader.length(), fileSize);
        Assert.assertEqual(leHeader.name(), "LE");

        // FO (LF chunk starts with length (4), type (2) and room number (2) – after that the room starts)
        final BasicChunk foChunk = bb.readChunk();

        final Map<Integer, LecLfIndex> roomOffsets = ChunkFODecoder.decode(foChunk).items().stream()
            .map(fo -> new LecLfIndex(fo.roomId(), fo.lfOffset(), fo.lfOffset() + 8))
            .collect(Collectors.toMap(LecLfIndex::roomId, idx -> idx));

        return new LecIndex(roomOffsets);
    }

    public LecIndex getLecIndex() {
        return lecIndex;
    }

    ByteString read(final int offset, final int len) {
        try {
            Preconditions.checkArgument(fileSize >= offset + len);
            final byte[] data = IOUtil.decryptInputStream(Channels.newInputStream(file.position(offset)), (byte) 0x69)
                .readNBytes(len);

            Assert.assertThat(data.length == len);

            return ByteString.wrap(data);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BasicChunk readChunk(final int offset) {
        return readData(offset).ebbLE().readChunk();
    }

    private ByteString readData(final int offset) {
        final EnhancedByteBuffer header = read(offset, 4).ebbLE();
        final int blksize = header.readU32();
        Assert.assertRange(blksize, 6, 1 << 21);

        return read(offset, blksize);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

}
