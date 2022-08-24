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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;

import de.siegmar.jmonkey.commons.io.BasicChunk;
import de.siegmar.jmonkey.datarepository.DataRepository;
import de.siegmar.jmonkey.decoder.script.PrintOpcodeDelegate;
import de.siegmar.jmonkey.decoder.script.Script;
import de.siegmar.jmonkey.decoder.script.ScriptDecoder;
import de.siegmar.jmonkey.explorer.util.Resource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

public class ScriptViewController {

    @FXML
    private TextArea textArea;

    private DataRepository dataRepository;

    public void setDataRepository(final DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void setScript(final int scriptId) {
        final BasicChunk scriptData = dataRepository.readGlobalScript(scriptId);

        final StringWriter sw = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(sw);
        final PrintOpcodeDelegate opcodeDelegate = new PrintOpcodeDelegate(printWriter::println);
        final ScriptDecoder scriptDecoder = new ScriptDecoder();

        final Script script = new Script(-1, scriptDecoder, opcodeDelegate, scriptData.data(), 0,
            List.of(), false, false);
        script.execute();

        textArea.setText(sw.toString());
    }

    public static Pane load(final DataRepository dataRepository, final int scriptId) {
        final FXMLLoader fxmlLoader = new FXMLLoader(Resource.url("/jfxml/script_viewer.fxml"));
        final Pane scriptPane;
        try {
            scriptPane = fxmlLoader.load();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        final ScriptViewController controller = fxmlLoader.getController();
        controller.setDataRepository(dataRepository);
        controller.setScript(scriptId);

        return scriptPane;
    }

}
