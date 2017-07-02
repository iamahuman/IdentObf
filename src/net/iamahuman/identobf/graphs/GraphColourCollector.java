/*
 * Copyright (c) 2017 Kang Jinoh <jinoh.kang.kr@gmail.com>. All rights reserved.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.iamahuman.identobf.graphs;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import net.iamahuman.identobf.nodes.ColourNamer;
import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.user.ConstraintException;

import java.util.*;

/**
 * Created by luke1337 on 6/13/17.
 */
public class GraphColourCollector implements GraphColourResultCallback {

    private final Set<Node> colouredNodes = new HashSet<>();
    private final Map<Integer, Object> forcedColours = new HashMap<>();
    private boolean isComplete = false, isUnsatisfied = false;
    private int chroma;
    private static final boolean DEBUG = true;

    @Override
    public synchronized void onColour(Collection<Node> colouredNodes, int chroma) {
        if (isComplete || isUnsatisfied)
            throw new IllegalStateException("this collector is frozen");
        if (this.chroma < chroma)
            this.chroma = chroma;
        this.colouredNodes.addAll(colouredNodes);
        for (Node n : colouredNodes) {
            assert n.colourIndex != -1;
            if (n.forcedColourIdentifier != null) {
                if (GraphColourCollector.class.desiredAssertionStatus()) {
                    Object old = forcedColours.get(n.colourIndex);
                    if (old != null && !old.equals(n.forcedColourIdentifier))
                        throw new AssertionError();
                }
                forcedColours.put(n.colourIndex, n.forcedColourIdentifier);
            }
        }
    }

    @Override
    public synchronized void onComplete() {
        if (isComplete || isUnsatisfied)
            throw new IllegalStateException("this collector is frozen");
        isComplete = true;
    }

    @Override
    public synchronized void onUnsatisfied() {
        if (isComplete || isUnsatisfied)
            throw new IllegalStateException("this collector is frozen");
        isUnsatisfied = true;
    }

    private void checkComplete() throws ConstraintException {
        if (isUnsatisfied)
            throw new ConstraintException("no satisfying instance found");
        if (!isComplete)
            throw new IllegalStateException("not yet frozen");
    }

    public synchronized <T> Map<Item, T> getColourMap(ColourNamer<? extends T> namer) throws ConstraintException {
        checkComplete();
        Map<Item, T> map = new HashMap<>();
        for (Node node : colouredNodes) {
            T colour = namer.identifierFor(node, forcedColours);
            for (Item item : node.items)
                map.put(item, colour);
        }
        return map;
    }

    public int getChroma() throws ConstraintException {
        checkComplete();
        return chroma;
    }
}
