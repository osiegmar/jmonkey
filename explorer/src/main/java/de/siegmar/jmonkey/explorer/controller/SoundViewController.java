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

package de.siegmar.jmonkey.explorer.controller;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.commons.io.BasicChunkHeader;
import de.siegmar.jmonkey.commons.io.ByteString;
import de.siegmar.jmonkey.commons.io.EnhancedByteBuffer;
import de.siegmar.jmonkey.datarepository.DataRepository;
import de.siegmar.jmonkey.decoder.sound.ChunkADDecoder;
import de.siegmar.jmonkey.explorer.misc.MidiPlayer;
import de.siegmar.jmonkey.explorer.util.Resource;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;

public class SoundViewController {

    private final MidiPlayer midiPlayer;
    private DataRepository dataRepository;
    private int soundId;

    public SoundViewController() throws MidiUnavailableException {
        midiPlayer = new MidiPlayer();
    }

    public void setDataRepository(final DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void setSoundId(final int soundId) {
        this.soundId = soundId;
    }

    public static Pane load(final DataRepository dataRepository, final int soundId) {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/sound_viewer.fxml"));
        final Pane soundPane;
        try {
            soundPane = fxmlLoader.load();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        final SoundViewController controller = fxmlLoader.getController();
        controller.setDataRepository(dataRepository);
        controller.setSoundId(soundId);

        return soundPane;
    }

    public void play(final ActionEvent e) throws InvalidMidiDataException, IOException {
        final BasicChunk soundChunk = dataRepository.readSound(soundId);

        if ("RO".equals(soundChunk.header().name())) {
            new Alert(Alert.AlertType.ERROR, "Roland sound not supported", ButtonType.CLOSE).show();
            return;
        } else if ("AM".equals(soundChunk.header().name())) {
            new Alert(Alert.AlertType.ERROR, "Amiga sound not supported", ButtonType.CLOSE).show();
            return;
        } else if (!"SO".equals(soundChunk.header().name())) {
            new Alert(Alert.AlertType.ERROR, "Unknown sound type: "
                + soundChunk.header().name(), ButtonType.CLOSE).show();
            return;
        }

        final EnhancedByteBuffer bb = soundChunk.ebbLE();

        final BasicChunkHeader waChunk = bb.readChunkHeader();
        bb.skip(waChunk.payloadLength());

        final BasicChunk adChunk = bb.readChunk();
        final ByteString data = ChunkADDecoder.exportMidi(adChunk);
        if (data == null) {
            new Alert(Alert.AlertType.ERROR, "SFX is currently not supported", ButtonType.CLOSE).show();
        } else {
            midiPlayer.playFile(data);
        }
    }

}
