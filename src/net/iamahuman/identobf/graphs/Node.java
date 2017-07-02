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

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.Reference;
import net.iamahuman.identobf.nodes.RequestMeta;
import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.ConstraintException;

import java.util.*;

/**
 * Created by luke1337 on 6/13/17.
 */
public class Node implements ConnectedGraph.Owned {

    private static final int DEFERRED_CHOICE_1 = 0;

    ConnectedGraph owner = null;
    Collection<Item> items = new ArrayList<>();
    Set<Node> edges = new HashSet<>();
    Set<Clique> coverers = new HashSet<>();
    Object forcedColourIdentifier = null;
    int colourIndex = -1;
    int workvecIndex = -1;

    Node() {
    }

    @Override
    public ConnectedGraph getOwner() {
        return owner;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public Set<Node> getEdges() {
        return edges;
    }

    public Set<Clique> getCoverers() {
        return coverers;
    }

    public Object getForcedColourIdentifier() {
        return forcedColourIdentifier;
    }

    public int getColourIndex() {
        return colourIndex;
    }

    void connect(Node other) throws ConstraintException {
        if (other == null)
            throw new NullPointerException("other is null");

        if (owner != null || other.owner != null)
            throw new IllegalStateException("connecting usually not done when finalized");

        if (this.equals(other))
            throw new ConstraintException("found a loop");

        final Object myColour = forcedColourIdentifier;
        final Object yourColour = other.forcedColourIdentifier;
        if (myColour != null && yourColour != null) {
            if (myColour.equals(yourColour))
                throw new IllegalStateException("nodes with same colour should already have been merged into one.");
            if (Node.class.desiredAssertionStatus()) {
                FixedColourClique my = getFixedColourClique();
                if (!(my != null && my == other.getFixedColourClique()))
                    throw new AssertionError("not already connected?");
            }
            // no need to add an edge since they are already connected
        } else if (edges.add(other) ^ other.edges.add(this)) // make every edge bidirectional as of now
            throw new IllegalStateException("incoherent / partial edge add");
    }

    private FixedColourClique getFixedColourClique() {
        for (Clique c : coverers) {
            if (c instanceof FixedColourClique)
                return (FixedColourClique) c;
        }
        return null;
    }

    void validateShallow() {
        if (edges.contains(this))
            throw new IllegalStateException("edges contain self");
        if (edges.contains(null))
            throw new IllegalStateException("edges contain null");
        for (Clique c : coverers)
            if (!c.nodes.contains(this))
                throw new IllegalStateException("clique cover inconsistent");
    }

    public void validate() {
        validateShallow();
        for (Node e : edges)
            if (e.owner != this.owner)
                throw new IllegalStateException("edge owner mismatch");
        for (Clique c : coverers)
            if (c.owner != this.owner)
                throw new IllegalStateException("clique owner mismatch");
    }

    public boolean isTrivial() {
        // NOTE coverers.size() == 0 -> nontrivial
        return edges.isEmpty() && coverers.size() == 1;
    }
}
