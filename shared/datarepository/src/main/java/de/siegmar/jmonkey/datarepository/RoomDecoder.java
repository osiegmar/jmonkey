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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.decoder.room.ChunkCC;
import de.siegmar.jmonkey.decoder.room.ChunkCCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkEN;
import de.siegmar.jmonkey.decoder.room.ChunkENDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkEX;
import de.siegmar.jmonkey.decoder.room.ChunkEXDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkLC;
import de.siegmar.jmonkey.decoder.room.ChunkLCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkLS;
import de.siegmar.jmonkey.decoder.room.ChunkLSDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkNL;
import de.siegmar.jmonkey.decoder.room.ChunkNLDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkOC;
import de.siegmar.jmonkey.decoder.room.ChunkOCDecoder;
import de.siegmar.jmonkey.decoder.room.ChunkSA;
import de.siegmar.jmonkey.decoder.room.ChunkSADecoder;
import de.siegmar.jmonkey.decoder.room.ChunkSL;
import de.siegmar.jmonkey.decoder.room.ChunkSLDecoder;
import de.siegmar.jmonkey.decoder.room.ObjectImageMeta;
import de.siegmar.jmonkey.decoder.room.ObjectItem;
import de.siegmar.jmonkey.decoder.room.Room;
import de.siegmar.jmonkey.decoder.room.box.ChunkBX;
import de.siegmar.jmonkey.decoder.room.box.ChunkBXDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkBMDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkHD;
import de.siegmar.jmonkey.decoder.room.image.ChunkHDDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkOIDecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkPADecoder;
import de.siegmar.jmonkey.decoder.room.image.ChunkSPDecoder;
import de.siegmar.jmonkey.decoder.room.image.LayeredImage;

public final class RoomDecoder {

    private RoomDecoder() {
    }

    public static Room decodeRoom(final BasicChunk data) {
        Assert.assertEqual(data.header().name(), "RO");
        return new RoomBuilder(data).build();
    }

    private static class RoomBuilder {

        private final EnhancedByteBuffer bb;
        private final OnceRef<ChunkHD> chunkHD = new OnceRef<>();
        private final OnceRef<ChunkBX> chunkBX = new OnceRef<>();
        private final OnceRef<ChunkSA> chunkSA = new OnceRef<>();
        private final OnceRef<BasicChunk> parkBM = new OnceRef<>();
        private final Map<ObjectImageMeta, BasicChunk> parkOI = new HashMap<>();
        private final OnceRef<ChunkNL> chunkNL = new OnceRef<>();
        private final OnceRef<ChunkSL> chunkSL = new OnceRef<>();
        private final List<ChunkOC> chunkOC = new ArrayList<>();
        private final OnceRef<ChunkLC> chunkLC = new OnceRef<>();
        private final OnceRef<ChunkCC> chunkCC = new OnceRef<>();
        private final OnceRef<ColorPalette> chunkSP = new OnceRef<>();
        private final OnceRef<ColorPalette> chunkPA = new OnceRef<>();
        private final OnceRef<ChunkEN> chunkEN = new OnceRef<>();
        private final OnceRef<ChunkEX> chunkEX = new OnceRef<>();
        private final List<ChunkLS> chunkLS = new ArrayList<>();

        RoomBuilder(final BasicChunk data) {
            bb = data.ebbLE();
        }

        @SuppressWarnings("checkstyle:CyclomaticComplexity")
        public Room build() {
            while (bb.hasRemaining()) {
                final BasicChunk chunk = bb.readChunk();
                switch (chunk.header().name()) {
                    case "HD" -> chunkHD.set(ChunkHDDecoder.decode(chunk));
                    case "BX" -> chunkBX.set(ChunkBXDecoder.decode(chunk));
                    case "SA" -> chunkSA.set(ChunkSADecoder.decode(chunk));
                    case "BM" -> parkBM.set(chunk);
                    case "OI" -> appendOI(chunk);
                    case "NL" -> chunkNL.set(ChunkNLDecoder.decode(chunk));
                    case "SL" -> chunkSL.set(ChunkSLDecoder.decode(chunk));
                    case "OC" -> chunkOC.add(ChunkOCDecoder.decode(chunk));
                    case "LC" -> chunkLC.set(ChunkLCDecoder.decode(chunk));
                    case "CC" -> chunkCC.set(ChunkCCDecoder.decode(chunk));
                    case "SP" -> chunkSP.set(ChunkSPDecoder.readPalette(chunk));
                    case "PA" -> chunkPA.set(ChunkPADecoder.readPalette(chunk));
                    case "EN" -> chunkEN.set(ChunkENDecoder.decode(chunk));
                    case "EX" -> chunkEX.set(ChunkEXDecoder.decode(chunk));
                    case "LS" -> chunkLS.add(ChunkLSDecoder.decode(chunk));
                    default -> throw new IllegalStateException("Unknown: " + chunk.header().name());
                }
            }

            final ChunkHD hd = chunkHD.get().orElseThrow();
            final Optional<ColorPalette> colorPalette = chunkPA.get();

            final Optional<LayeredImage> bmImage = parkBM.get()
                .flatMap(chunk -> ChunkBMDecoder.decode(chunk, hd, colorPalette.orElse(null)));

            final List<ObjectItem> objectItems = buildObjectItems(chunkOC, parkOI, colorPalette.orElse(null));

            return new Room(
                hd,
                colorPalette,
                parkBM.get(),
                bmImage,
                chunkBX.get(),
                chunkSA.get(),
                chunkCC.get(),
                objectItems,
                chunkEN.get().orElseThrow(),
                chunkEX.get().orElseThrow(),
                chunkLS);
        }

        private List<ObjectItem> buildObjectItems(final List<ChunkOC> lchunkOC,
                                                  final Map<ObjectImageMeta, BasicChunk> lparkOI,
                                                  final ColorPalette palette) {
            final List<ObjectItem> items = new ArrayList<>();

            for (final ChunkOC objectCode : lchunkOC) {
                final BasicChunk chunkOI = lparkOI.get(new ObjectImageMeta(objectCode.objectId()));
                final Optional<LayeredImage> layeredImage = ChunkOIDecoder.decodeImage(chunkOI, objectCode, palette);
                items.add(new ObjectItem(objectCode, layeredImage));
            }

            return items;
        }

        private BasicChunk appendOI(final BasicChunk chunk) {
            final ObjectImageMeta key = ChunkOIDecoder.decodeImageMeta(chunk);
            if (parkOI.containsKey(key)) {
                throw new IllegalStateException("OI does already exist");
            }
            return parkOI.put(key, chunk);
        }

    }

    private static class OnceRef<T> {

        private T ref;

        private void set(final T obj) {
            if (ref != null) {
                throw new IllegalStateException("Reference already set");
            }
            ref = obj;
        }

        private Optional<T> get() {
            return Optional.ofNullable(ref);
        }

    }

}
