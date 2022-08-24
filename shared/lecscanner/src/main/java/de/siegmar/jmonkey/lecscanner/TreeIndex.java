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

package de.siegmar.jmonkey.lecscanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TreeIndex<T> implements Iterable<TreeIndex<T>> {

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final T chunk;
    private final List<TreeIndex<T>> children = new ArrayList<>();
    private TreeIndex<T> parent;

    public TreeIndex(final T chunk) {
        this.chunk = chunk;
    }

    // TODO rename
    public T chunk() {
        return chunk;
    }

    public List<TreeIndex<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(final TreeIndex<T> treeIndex) {
        treeIndex.setParent(this);
        children.add(treeIndex);
    }

    public TreeIndex<T> getParent() {
        return parent;
    }

    private void setParent(final TreeIndex<T> parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent already set");
        }
        this.parent = parent;
    }

    @Override
    public Iterator<TreeIndex<T>> iterator() {
        return children.iterator();
    }

    public Stream<TreeIndex<T>> deepStream() {
        return deepStream(this);
    }

    private Stream<TreeIndex<T>> deepStream(final TreeIndex<T> parentNode) {
        return parentNode.getChildren().stream()
            .map(this::deepStream)
            .reduce(Stream.of(parentNode), Stream::concat);
    }

    public Optional<TreeIndex<T>> findFirstBy(final Predicate<TreeIndex<T>> predicate) {
        return children.stream().filter(predicate).findFirst();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TreeIndex.class.getSimpleName() + "[", "]")
            .add("chunk=" + chunk)
            .toString();
    }

}
