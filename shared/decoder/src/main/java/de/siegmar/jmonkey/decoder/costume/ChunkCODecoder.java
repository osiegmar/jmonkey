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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;
import de.siegmar.jmonkey.commons.misc.ColorPalette;
import de.siegmar.jmonkey.commons.misc.WritableRasterImage;

/**
 * Costume > Animations > Limbs > Commands > Images
 *
 * - A costume consists of X animations.
 * - An animation is something like talking, walking – one animation for every direction
 * - Each animation is made up of up to 16 limbs – each limb defines a series of commands/images
 *
 * - Every limb that is used (mask is 1) defines a command offset address.
 *   If the offset address is 0xFFFF the limb is skipped. Otherwise, it is the start
 *   address of the command array and a length and looop-info is specified.
 *   The limb command (start address + length) defines the commands or images for that limb.
 *
 * - Commands array size is 1 - 161
 *
 * Example:
 * Guybrush costume
 * Walk animation > 3 limbs (Head, Torso, Feed)
 *  > Head is only one command->image [1] that loops
 *  > Torso is made up of 2 commands->images [A, B] that loops
 *  > Feed is made up of 2 commands->images [X, Y, Z] that loops
 *
 *  Animation:
 *  Frame 1: [1 & A & X]
 *  Frame 2: [1 & B & Y]
 *  Frame 3: [1 & A & Z]
 *  Repeat with Frame 1
 *
 *
 *  What if command is stop/start/hide/sound ?
 *  stop -> the limb is stopped
 */
public final class ChunkCODecoder {

    private ChunkCODecoder() {
    }

    public static Costume decode(final BasicChunk chunk, final ColorPalette roomPalette) {
        Assert.assertEqual(chunk.header().name(), "CO");

        return new CostumeBuilder(chunk, roomPalette).build();
    }

    @SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
    private static class CostumeBuilder {

        private static final int TOTAL_LIMBS = 16;

        private final BasicChunk chunk;
        private final ColorPalette roomPalette;
        private final EnhancedByteBuffer bb;

        private final CostumeHeader header;
        private final LimbImageDecoder limbImageDecoder;

        CostumeBuilder(final BasicChunk chunk, final ColorPalette roomPalette) {
            this.chunk = chunk;
            this.roomPalette = roomPalette;
            header = decodeHeader(chunk, roomPalette);
            limbImageDecoder = new LimbImageDecoder(chunk);
            bb = chunk.dataWithHeader().ebbLE();
        }

        // FIXME roomPalette should be used later, palette can be changed on existing costumes
        private static CostumeHeader decodeHeader(final BasicChunk chunk, final ColorPalette roomPalette) {
            final EnhancedByteBuffer bb = chunk.ebbLE();

            final int numAnim = bb.readU8() + 1;
            final int tmpFormat = bb.readU8();
            final boolean mirror = (tmpFormat & 0x80) == 0;
            final int format = tmpFormat & 0x7F;

            final int colors = switch (format) {
                case 0x58 -> 16;
                case 0x59 -> 32;
                default -> throw new IllegalStateException("Unknown format: " + format);
            };

            final var palette = mapPalette(roomPalette, bb.readImmutableBytes(colors));
            final int animCommandsOffset = bb.readU16();

            final var limbsOffsets = bb.readList(TOTAL_LIMBS, () -> new LimbOffset(bb.readU16()));
            final var animOffsets = bb.readList(numAnim, () -> new AnimOffset(bb.readU16()));

            Assert.assertEqual(bb.position(), animOffsets.stream()
                .filter(AnimOffset::isDefined)
                .min(AnimOffset::compareTo)
                .orElseThrow().address());

            // read animCommands
            bb.position(animCommandsOffset);
            final int firstLimbOffset = limbsOffsets.get(0).address();
            final int cmdSize = firstLimbOffset - animCommandsOffset;
            final var animCommands =
                bb.readList(cmdSize, () -> new CostumeAnimationCommand(bb.readU8()));

            Assert.assertEqual(bb.position(), firstLimbOffset);

            return new CostumeHeader(mirror, palette, limbsOffsets, animOffsets, animCommands);
        }

        public Costume build() {
            final var animations = header.uniqueAnimOffsets().stream()
                .collect(Collectors.toMap(animOffset -> animOffset, this::decodeLimbAnimation, (a, b) -> b));

            return new Costume(header, animations);
        }

        private static ColorPalette mapPalette(final ColorPalette roomPalette, final ByteString costumePalette) {
            final byte[] palette = costumePalette.dumpCopy();
            return new ColorPalette(IntStream.range(0, palette.length)
                .map(i -> roomPalette.color(palette[i] & 0xff))
                .toArray());
        }

        private List<LimbAnimation> decodeLimbAnimation(final AnimOffset animOffset) {
            bb.position(animOffset.address());

            final List<LimbAnimation> limbAnimations = new ArrayList<>();
            final int limbMask = bb.readU16();
            for (int limbNo = 0; limbNo < TOTAL_LIMBS; limbNo++) {
                if ((limbMask & (0x8000 >> limbNo)) == 0) {
                    continue;
                }

                final int start = bb.readU16();
                if (start == 0xFFFF) {
                    // TODO why is the limbMask not 0 then?
                    continue;
                }

                final int tmpDef = bb.readU8();
                final boolean loop = (tmpDef & 0x80) == 0;
                final int framesLen = (tmpDef & 0x7f) + 1;

                final List<LimbFrame> limbFrames = new ArrayList<>(framesLen);

                // a frame can be either a command (like start/stop/hide/sound) or an image
                for (int frameNo = 0; frameNo < framesLen; frameNo++) {
                    final CostumeAnimationCommand command = header.animationCommands().get(start + frameNo);
                    limbFrames.add(buildLimbFrame(limbNo, command));
                }

                limbAnimations.add(new LimbAnimation(limbNo, start, loop, limbFrames));
            }

            return Collections.unmodifiableList(limbAnimations);
        }

        private LimbFrame buildLimbFrame(final int limbNo, final CostumeAnimationCommand command) {
            return command.isControl()
                ? new LimbFrame(command)
                : new LimbFrame(command, limbImageDecoder.obtainImage(resolvePicOffset(limbNo, command)));
        }

        private ImageOffset resolvePicOffset(final int limbNo, final CostumeAnimationCommand command) {
            final int imageTableOffsetStart = header.limbOffsets().get(limbNo).address();
            final int picTableOffset = imageTableOffsetStart + command.getCommand() * 2;
            return new ImageOffset(bb.readU16(picTableOffset));
        }

        private record ImageOffset(int address) {

        }

        private class LimbImageDecoder {

            private final EnhancedByteBuffer bb;
            private final int shift;
            private final int mask;
            private final Map<ImageOffset, CostumeLimbImage> imageCache = new HashMap<>();

            LimbImageDecoder(final BasicChunk chunk) {
                bb = chunk.dataWithHeader().ebbLE();

                switch (header.palette().size()) {
                    case 16 -> {
                        shift = 4;
                        mask = 0xF;
                    }
                    case 32 -> {
                        shift = 3;
                        mask = 0x7;
                    }
                    default -> throw new IllegalArgumentException();
                }
            }

            private CostumeLimbImage obtainImage(final ImageOffset offset) {
                return imageCache.computeIfAbsent(offset, this::decodeImage);
            }

            private CostumeLimbImage decodeImage(final ImageOffset imageOffset) {
                bb.position(imageOffset.address());

                final int width = bb.readU16();
                final int height = bb.readU16();
                final int relX = bb.readS16();
                final int relY = bb.readS16();
                final int moveX = bb.readS16();
                final int moveY = bb.readS16();

                Assert.assertThat(width <= 255, "width: %d", width);
                Assert.assertThat(height <= 255, "height: %d", height);

                final WritableRasterImage img = new WritableRasterImage(width, height);

                int x = 0;
                int y = 0;

                IMAGE:
                while (true) {
                    int rep = bb.readU8();
                    final int color = rep >> shift;
                    rep &= mask;
                    if (rep == 0) {
                        rep = bb.readU8();
                    }
                    while (rep > 0) {
                        if (color != 0) {
                            img.draw(x, y, header.palette().color(color));
                        }
                        rep--;
                        y++;
                        if (y >= height) {
                            y = 0;
                            x++;
                            if (x >= width) {
                                break IMAGE;
                            }
                        }
                    }
                }

                return new CostumeLimbImage(img.rasterImage(), relX, relY, moveX, moveY);
            }

        }

    }

}
