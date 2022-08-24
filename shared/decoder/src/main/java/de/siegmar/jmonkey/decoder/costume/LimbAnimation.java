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

import java.util.List;
import java.util.StringJoiner;

public class LimbAnimation {

    private final int limbNo;
    private final int start;
    private final boolean loop;
    private final List<LimbFrame> frames;

    public LimbAnimation(final int limbNo, final int start, final boolean loop, final List<LimbFrame> frames) {
        this.limbNo = limbNo;
        this.start = start;
        this.loop = loop;
        this.frames = List.copyOf(frames);
    }

    public int getLimbNo() {
        return limbNo;
    }

    public int getStart() {
        return start;
    }

    public boolean isLoop() {
        return loop;
    }

    public List<LimbFrame> getFrames() {
        return frames;
    }

    public LimbFrame getFrame(final int frame) {
        final int framePos;
        if (frame < frames.size()) {
            framePos = frame;
        } else {
            if (!loop) {
                framePos = frames.size() - 1;
            } else {
                framePos = frame % frames.size();
            }
        }

        return frames.get(framePos);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LimbAnimation.class.getSimpleName() + "[", "]")
            .add("limbNo=" + limbNo)
            .add("start=" + start)
            .add("loop=" + loop)
            .add("frames=" + frames)
            .toString();
    }

}
