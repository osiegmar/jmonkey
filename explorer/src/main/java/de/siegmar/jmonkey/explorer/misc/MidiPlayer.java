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

package de.siegmar.jmonkey.explorer.misc;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import de.siegmar.jmonkey.commons.io.ByteString;

public class MidiPlayer {

    private final Sequencer sequencer;

    public MidiPlayer() throws MidiUnavailableException {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
    }

    public synchronized void playFile(final ByteString data) throws InvalidMidiDataException, IOException {
        if (sequencer.isRunning()) {
            sequencer.stop();
        }

        final Sequence seq = MidiSystem.getSequence(data.asInputStream());
        System.out.format("Play midi; division type: %.2f, patches: %d, tracks: %d, "
                + "resolution: %d, length: %d, tick length: %d",
            seq.getDivisionType(), seq.getPatchList().length, seq.getTracks().length,
            seq.getResolution(), seq.getMicrosecondLength(), seq.getTickLength()
        );

        // pass the sequence to the sequencer
        sequencer.setSequence(seq);

        // start the sequencer
        sequencer.start();
    }

}
