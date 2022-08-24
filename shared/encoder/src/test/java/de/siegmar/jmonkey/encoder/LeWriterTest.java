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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.siegmar.jmonkey.commons.io.ByteString;

class LeWriterTest {

    @Test
    void emptyNode() throws IOException {
        final Node n = Node.createRoot("NO");
        assertThat(n.size()).isEqualTo(6);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        n.writeTo(new LeWriter(bos));

        assertThat(bos.toByteArray()).isEqualTo(new byte[]{
            6, 0, 0, 0, (byte) 'N', (byte) 'O',
        });
    }

    @Test
    void withData() throws IOException {
        final Node n = Node.createRoot("NO");
        n.setData(ByteString.wrap(new byte[]{0x0A, 0x0B, }));
        assertThat(n.size()).isEqualTo(8);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        n.writeTo(new LeWriter(bos));

        assertThat(bos.toByteArray()).isEqualTo(new byte[]{
            8, 0, 0, 0, (byte) 'N', (byte) 'O', 0x0A, 0x0B,
        });
    }

    @Test
    void withChild() throws IOException {
        final Node n = Node.createRoot("NO");

        final Node children = n.createChild("CH");
        children.setData(ByteString.wrap(new byte[]{0x0A, 0x0B, }));

        assertThat(n.size()).isEqualTo(14);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        n.writeTo(new LeWriter(bos));

        assertThat(bos.toByteArray()).isEqualTo(new byte[]{
            14, 0, 0, 0, (byte) 'N', (byte) 'O',
            8, 0, 0, 0, (byte) 'C', (byte) 'H', 0x0A, 0x0B,
        });
    }

    @Test
    void withDataAndChild() throws IOException {
        final Node n = Node.createRoot("NO");
        n.setData(ByteString.wrap(new byte[]{1, 2}));

        final Node children = n.createChild("CH");
        children.setData(ByteString.wrap(new byte[]{0x0A, 0x0B }));

        assertThat(n.size()).isEqualTo(16);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        n.writeTo(new LeWriter(bos));

        assertThat(bos.toByteArray()).isEqualTo(new byte[]{
            16, 0, 0, 0, (byte) 'N', (byte) 'O', 1, 2,
            8, 0, 0, 0, (byte) 'C', (byte) 'H', 0x0A, 0x0B,
        });
    }

}
