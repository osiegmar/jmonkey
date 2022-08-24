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

package de.siegmar.jmonkey.decoder.sound;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.commons.lang.Assert;

@SuppressWarnings({
    "checkstyle:TrailingComment",
    "checkstyle:CyclomaticComplexity",
    "checkstyle:JavaNCSS",
    "checkstyle:ExecutableStatementCount"
})
public final class ChunkADDecoder {

    private static final byte[] ADLIB_INSTR_MIDI_HACK = {
        0x00, (byte) 0xf0, 0x14, 0x7d, 0x00,            // sysex 00: part on/off
        0x00, 0x00, 0x03,                               // part/channel  (offset  5)
        0x00, 0x00, 0x07, 0x0f, 0x00, 0x00, 0x08, 0x00,
        0x00, 0x00, 0x00, 0x02, 0x00, 0x00, (byte) 0xf7,
        0x00, (byte) 0xf0, 0x41, 0x7d, 0x10,            // sysex 16: set instrument
        0x00, 0x01,                                     // part/channel  (offset 28)
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, (byte) 0xf7,
        0x00, (byte) 0xb0, 0x07, 0x64,                  // Controller 7 = 100 (offset 92)
    };

    private static final int PPQN = 480;
//    private static final boolean REPEAT_ENABLED = false;

    private ChunkADDecoder() {
    }

    @SuppressWarnings("checkstyle:NestedIfDepth")
    public static ByteString exportMidi(final BasicChunk chunk) {
        Assert.assertEqual(chunk.header().name(), "AD");

        final EnhancedByteBuffer in = chunk.ebbLE();

        // ignore first two bytes
        in.position(2);

        final int type = in.readU8();

        final ByteBuffer out = ByteBuffer.allocate(100000); // TODO change this fixed allocation

        final int sizePos;

        if (type == 0x80) {
            sizePos = writeMidiHeader(out);

            final int ticks = in.readU8();

            final int playOnce = in.readU8();
            if (playOnce < 0 || playOnce > 1) {
                throw new IllegalStateException();
            }

            // ignore 5 bytes
            in.position(in.position() + 5);

            final int noInstruments = in.readU8();

//        if (noInstruments != 8) {
//             TODO remove, only for debugging / validation
//            throw new IllegalStateException("Invalid number of instruments: " + noInstruments);
//        }

            final byte[] channels = in.readBytes(8);

            final byte[] instruments = in.readBytes(128);

            final int dw = 500000 * 256 / ticks;

            // midi event to set tempo
            out.put(new byte[]{0x00, (byte) 0xff, 0x51, 0x03});
            out.put((byte) ((dw >> 16) & 0xFF));
            out.put((byte) ((dw >> 8) & 0xFF));
            out.put((byte) (dw & 0xFF));

            // set instruments (TODO remove hacky)
            for (int i = 0; i < noInstruments; i++) {
                final int ch = channels[i] - 1;
                if (ch < 0 || ch > 15) {
                    throw new IllegalStateException();
//                continue;
                }

                out.put(prepareInstrument(instruments, i, ch));
            }

            // add pause before play begins
            if (PPQN / 3 >= 128) {
                out.put((byte) (PPQN / 3 >> 7 | 0x80));
            }
            out.put((byte) (PPQN / 3 & 0x7f));

            // copy over rest of input data
            in.copyTo(out);

            if (playOnce == 0) {
//            System.out.println("write multi");
//            out.flip();
                out.position(0);

                // The song is meant to be looped. We achieve this by inserting just
                // before the song end a jump to the song start. More precisely we abuse
                // a S&M sysex, "maybe_jump" to achieve this effect. We could also
                // use a set_loop sysex, but it's a bit longer, a little more complicated,
                // and has no advantage either.

                // First, find the track end
                while (out.hasRemaining()) {
                    if ((out.get() & 0xff) == 0xff && (out.get() & 0xff) == 0x2f) {
                        out.position(out.position() - 2);
                        break;
                    }
                }
//            final int endPos = out.position();
//            out.flip();
//            out.position(endPos);

                // Now insert the jump. The jump offset is measured in ticks.
                // We have ppqn/3 ticks before the first note.

                final int jumpOffset = PPQN / 3;
                out.put(new byte[]{(byte) 0xf0, 0x13, 0x7d, 0x30, 0x00}); // maybe_jump
                out.put(new byte[]{(byte) 0x00, 0x00}); // cmd -> 0 means always jump
                out.put(new byte[]{(byte) 0x00, 0x00, 0x00, 0x00}); // track -> there is only one track, 0
                out.put(new byte[]{(byte) 0x00, 0x00, 0x00, 0x01}); // beat -> for now, 1 (first beat)
                // Ticks
                out.put((byte) ((jumpOffset >> 12) & 0x0F));
                out.put((byte) ((jumpOffset >> 8) & 0x0F));
                out.put((byte) ((jumpOffset >> 4) & 0x0F));
                out.put((byte) (jumpOffset & 0x0F));
                out.put(new byte[]{(byte) 0x00, (byte) 0xf7}); // sysex end marker
            }
        } else {
            // TODO SFX is currently not supported
            return null;
        }

//            System.out.println("tempo: " + tempo);

        // end of track
//        out.put(new byte[] { 0x00, (byte) 0xff, 0x2f, 0x00, 0x00});
        out.put(new byte[]{0x00, (byte) 0xff, 0x2f, 0x00});

        // find end (clean input headers) // TODO remove?
//        out.position(0);
//        while (out.hasRemaining()) {
//            if ((out.get() & 0xff) == 0xff && (out.get() & 0xff) == 0x2f) {
//                break;
//            }
//        }

        // write length in header
        final int sizeOfTrack = out.position() - sizePos - 4; // subtract 4 because of the length chunk itself
        out.putInt(sizePos, sizeOfTrack);
//        System.out.format("sizepos: %d, endPos: %d, length: %d%n",
//            sizePos, endPosition, sizeOfTrack);

        out.flip();
        final byte[] data = new byte[out.limit()];
        out.get(data);

        return ByteString.wrap(data);
    }

    private static int writeMidiHeader(final ByteBuffer out) {
        out.put("MThd".getBytes(StandardCharsets.US_ASCII));
        out.putInt(6); // header length
        out.putChar((char) 0); // format 0: one, single multi-channel track
        out.putChar((char) 1); // one track

        // Pulses Per Quarter Note
        out.put((byte) (PPQN >> 8));
        out.put((byte) (PPQN & 0xff));

        out.put("MTrk".getBytes(StandardCharsets.US_ASCII));
        final int sizePos = out.position();
        out.putInt(0); // length will be filled later
        return sizePos;
    }

    private static byte[] prepareInstrument(final byte[] instr, final int i, final int ch) {
        final byte[] ptr = ADLIB_INSTR_MIDI_HACK.clone();

        ptr[5] += ch;
        ptr[28] += ch;
        ptr[92] += ch;

        /* mod_characteristics */
        ptr[30 + 0] = (byte) ((instr[i * 16 + 3] >> 4) & 0xf);
        ptr[30 + 1] = (byte) (instr[i * 16 + 3] & 0xf);

        /* mod_scalingOutputLevel */
        ptr[30 + 2] = (byte) ((instr[i * 16 + 4] >> 4) & 0xf);
        ptr[30 + 3] = (byte) (instr[i * 16 + 4] & 0xf);

        /* mod_attackDecay */
        ptr[30 + 4] = (byte) (((~instr[i * 16 + 5]) >> 4) & 0xf);
        ptr[30 + 5] = (byte) ((~instr[i * 16 + 5]) & 0xf);

        /* mod_sustainRelease */
        ptr[30 + 6] = (byte) (((~instr[i * 16 + 6]) >> 4) & 0xf);
        ptr[30 + 7] = (byte) ((~instr[i * 16 + 6]) & 0xf);

        /* mod_waveformSelect */
        ptr[30 + 8] = (byte) ((instr[i * 16 + 7] >> 4) & 0xf);
        ptr[30 + 9] = (byte) (instr[i * 16 + 7] & 0xf);

        /* car_characteristic */
        ptr[30 + 10] = (byte) ((instr[i * 16 + 8] >> 4) & 0xf);
        ptr[30 + 11] = (byte) (instr[i * 16 + 8] & 0xf);

        /* car_scalingOutputLevel */
        ptr[30 + 12] = (byte) ((instr[i * 16 + 9] >> 4) & 0xf);
        ptr[30 + 13] = (byte) (instr[i * 16 + 9] & 0xf);

        /* car_attackDecay */
        ptr[30 + 14] = (byte) (((~instr[i * 16 + 10]) >> 4) & 0xf);
        ptr[30 + 15] = (byte) ((~instr[i * 16 + 10]) & 0xf);

        /* car_sustainRelease */
        ptr[30 + 16] = (byte) (((~instr[i * 16 + 11]) >> 4) & 0xf);
        ptr[30 + 17] = (byte) ((~instr[i * 16 + 11]) & 0xf);

        /* car_waveFormSelect */
        ptr[30 + 18] = (byte) ((instr[i * 16 + 12] >> 4) & 0xf);
        ptr[30 + 19] = (byte) (instr[i * 16 + 12] & 0xf);

        /* feedback */
        ptr[30 + 20] = (byte) ((instr[i * 16 + 2] >> 4) & 0xf);
        ptr[30 + 21] = (byte) (instr[i * 16 + 2] & 0xf);
//                ptr += sizeof(ADLIB_INSTR_MIDI_HACK);
        return ptr;
    }

}
