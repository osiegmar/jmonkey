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

package de.siegmar.jmonkey.explorer.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.StringJoiner;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.DataChunkType;
import de.siegmar.jmonkey.commons.io.IOUtil;
import de.siegmar.jmonkey.commons.lang.Preconditions;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.costume.ChunkCODecoder;
import de.siegmar.jmonkey.decoder.costume.Costume;
import de.siegmar.jmonkey.decoder.header.ChunkFO;
import de.siegmar.jmonkey.decoder.header.ChunkFODecoder;
import de.siegmar.jmonkey.decoder.header.ChunkLF;
import de.siegmar.jmonkey.decoder.header.ChunkLFDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkCC;
import de.siegmar.jmonkey.decoder.room.ChunkCCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkLC;
import de.siegmar.jmonkey.decoder.room.ChunkLCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkNL;
import de.siegmar.jmonkey.decoder.room.ChunkNLDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ChunkOCDecoder;
import de.siegmar.jmonkey.decoder.room.ObjectImageMeta;
import de.siegmar.jmonkey.decoder.room.box.ChunkBX;
import de.siegmar.jmonkey.decoder.room.box.ChunkBXDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.ChunkHDDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkOIDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkPADecoder;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;
import de.siegmar.jmonkey.decoder.sound.ChunkADDecoder;
import de.siegmar.jmonkey.decoder.sound.ChunkAM;
import de.siegmar.jmonkey.decoder.sound.ChunkAMDecoder;
import de.siegmar.jmonkey.decoder.sound.ChunkWADecoder;
import de.siegmar.jmonkey.lecscanner.LecChunk;
import de.siegmar.jmonkey.lecscanner.LecScanner;
import de.siegmar.jmonkey.lecscanner.TreeIndex;

@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class LecDecoder implements Closeable {

    private final Path file;
    private final TreeIndex<LecChunk> treeIndex;
    private final FileChannel ch;

    public LecDecoder(final Path file) {
        this.file = file;
        treeIndex = LecScanner.scanTree(file);
        try {
            ch = FileChannel.open(file, StandardOpenOption.READ);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getFile() {
        return file;
    }

    public TreeIndex<LecChunk> getIndex() {
        return treeIndex;
    }

    public ChunkFO readFO(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.FO);
        return ChunkFODecoder.decode(sliceChunk(chunk));
    }

    public ChunkOC readOC(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.OC);
        return ChunkOCDecoder.decode(sliceChunk(chunk));
    }

    public ColorPalette readPA(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.PA);
        return ChunkPADecoder.readPalette(sliceChunk(chunk));
    }

    public ObjectImageMeta readOI(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.OI);
        return ChunkOIDecoder.decodeImageMeta(sliceChunk(chunk));
    }

    public Optional<LayeredImage> readOI(final TreeIndex<LecChunk> index) {
        Preconditions.checkArgument(index.chunk().type() == DataChunkType.OI);

        final BasicChunk oiChunk = sliceChunk(index.chunk());
        final ObjectImageMeta objectImageMeta = ChunkOIDecoder.decodeImageMeta(oiChunk);

        final TreeIndex<LecChunk> roomChunk = index.getParent();

        final ChunkOC chunkOC = roomChunk.deepStream()
            .filter(c -> c.chunk().type() == DataChunkType.OC)
            .map(c -> readOC(c.chunk()))
            .filter(oc -> oc.objectId() == objectImageMeta.objectId())
            .findFirst()
            .orElseThrow();

        if (chunkOC.height() == 0) {
            return Optional.empty();
        }

        final Optional<ColorPalette> vgaColorPalette = roomChunk
            .findFirstBy(b -> b.chunk().type() == DataChunkType.PA)
            .map(c -> readPA(c.chunk()));

        return ChunkOIDecoder.decodeImage(oiChunk, chunkOC, vgaColorPalette.orElse(null));
    }

    public ChunkNL readNL(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.NL);
        return ChunkNLDecoder.decode(sliceChunk(chunk));
    }

    public ChunkHD readHD(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.HD);
        return ChunkHDDecoder.decode(sliceChunk(chunk));
    }

    public Optional<LayeredImage> readBM(final TreeIndex<LecChunk> index) {
        Preconditions.checkArgument(index.chunk().type() == DataChunkType.BM);

        final TreeIndex<LecChunk> roomChunk = index.getParent();

        final ChunkHD chunkHD = roomChunk
            .findFirstBy(b -> b.chunk().type() == DataChunkType.HD)
            .map(hd -> readHD(hd.chunk()))
            .orElseThrow();

        final Optional<ColorPalette> colorPalette = roomChunk
            .findFirstBy(b -> b.chunk().type() == DataChunkType.PA)
            .map(p -> readPA(p.chunk()));

        final BasicChunk bmData = sliceChunk(index.chunk());

        return ChunkBMDecoder.decode(bmData, chunkHD, colorPalette.orElse(null));
    }

    public ChunkBX readBX(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.BX);
        return ChunkBXDecoder.decode(sliceChunk(chunk));
    }

    public ChunkLF readLF(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.LF);
        return ChunkLFDecoder.decode(sliceChunk(chunk));
    }

    public ChunkLC readLC(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.LC);
        return ChunkLCDecoder.decode(sliceChunk(chunk));
    }

    public ByteString readWA(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.WA);
        return ChunkWADecoder.decode(sliceChunk(chunk));
    }

    public ChunkAM readAM(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.AM);
        return ChunkAMDecoder.decode(sliceChunk(chunk));
    }

    public ByteString readAD(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.AD);
        return ChunkADDecoder.exportMidi(sliceChunk(chunk));
    }

    public Costume readCO(final TreeIndex<LecChunk> index) {
        Preconditions.checkArgument(index.chunk().type() == DataChunkType.CO);

        final TreeIndex<LecChunk> roomChunk = index.getParent();

        final ColorPalette colorPalette = roomChunk.deepStream()
            .filter(b -> b.chunk().type() == DataChunkType.PA)
            .findFirst()
            .map(c -> readPA(c.chunk()))
            .orElse(ColorPalette.EGA);

        return ChunkCODecoder.decode(sliceChunk(index.chunk()), colorPalette);
    }

    public ChunkCC readCC(final LecChunk chunk) {
        Preconditions.checkArgument(chunk.type() == DataChunkType.CC);
        return ChunkCCDecoder.decode(sliceChunk(chunk));
    }

    public BasicChunk sliceChunk(final LecChunk chunk) {
        try {
            ch.position(chunk.pos());
            return IOUtil.read(ch, chunk.length()).ebbLE().readChunk();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void export(final LecChunk chunk, final Path out) {
        try (OutputStream os = Files.newOutputStream(out)) {
            sliceChunk(chunk).dataWithHeader().writeTo(os);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LecDecoder.class.getSimpleName() + "[", "]")
            .add("file=" + file)
            .toString();
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

}
