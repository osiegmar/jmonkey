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

package de.siegmar.jmonkey.decoder.script;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import de.siegmar.jmonkey.decoder.script.parameter.OpParameter;

public class ScummString {

    private final List<StringPart> parts;

    public ScummString(final List<StringPart> parts) {
        this.parts = List.copyOf(parts);
    }

    public List<StringPart> getParts() {
        return parts;
    }

    public static ScummStringBuilder builder() {
        return new ScummStringBuilder();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ScummString.class.getSimpleName() + "[", "]")
            .add("parts=" + parts)
            .toString();
    }

    public interface StringPart {

    }

    public static class NewLinePart implements StringPart {

    }

    public static class NewLinePart2 implements StringPart {

    }

    public static class VerbNewLinePart implements StringPart {

    }

    public static class KeepTextPart implements StringPart {

    }

    public static class SleepPart implements StringPart {

    }

    public static class IntPart implements StringPart {

        private final OpParameter param;

        public IntPart(final OpParameter param) {
            this.param = param;
        }

        public OpParameter getParam() {
            return param;
        }

    }

    public static class VerbPart implements StringPart {

        private final OpParameter param;

        public VerbPart(final OpParameter param) {
            this.param = param;
        }

        public OpParameter getParam() {
            return param;
        }

    }

    public static class NamePart implements StringPart {

        private final OpParameter param;

        public NamePart(final OpParameter param) {
            this.param = param;
        }

        public OpParameter getParam() {
            return param;
        }

    }

    public static class GetStringPart implements StringPart {

        private final OpParameter param;

        public GetStringPart(final OpParameter param) {
            this.param = param;
        }

        public OpParameter getParam() {
            return param;
        }

    }

    public static class TextPart implements StringPart {

        private final byte[] rawString;
        private final String text;

        public TextPart(final byte[] rawString, final String text) {
            this.rawString = rawString.clone();
            this.text = text;
        }

        public byte[] getRawString() {
            return rawString.clone();
        }

        public String getText() {
            return text;
        }

    }

    @SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
    public static class ScummStringBuilder {

        private final List<StringPart> parts = new ArrayList<>();

        public ScummString build() {
            return new ScummString(parts);
        }

        public void newline() {
            parts.add(new NewLinePart());
        }

        public void newline2() {
            parts.add(new NewLinePart2());
        }

        public void keepText() {
            parts.add(new KeepTextPart());
        }

        public void sleep() {
            parts.add(new SleepPart());
        }

        public void getInt(final OpParameter param) {
            parts.add(new IntPart(param));
        }

        public void getVerb(final OpParameter param) {
            parts.add(new VerbPart(param));
        }

        public void getName(final OpParameter param) {
            parts.add(new NamePart(param));
        }

        public void getString(final OpParameter param) {
            parts.add(new GetStringPart(param));
        }

        public void verbNewline() {
            parts.add(new VerbNewLinePart());
        }

        public void text(final byte[] rawString, final String str) {
            parts.add(new TextPart(rawString, str));
        }

    }

}
