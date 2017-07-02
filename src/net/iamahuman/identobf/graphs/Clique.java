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

import net.iamahuman.identobf.nodes.Group;
import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.ItemGroups;
import net.iamahuman.identobf.nodes.Reference;
import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.ConstraintException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by luke1337 on 6/13/17.
 */
public abstract class Clique implements ConnectedGraph.Owned {
    ConnectedGraph owner = null;
    Set<Node> nodes = new HashSet<>();
    Set<Node> outgoingNodes = new HashSet<>();
    private static final boolean DEBUG = false;

    Clique() {
    }

    @Override
    public ConnectedGraph getOwner() {
        return owner;
    }

    public Collection<Node> getNodes() {
        return nodes;
    }

    public Collection<Node> getOutgoingNodes() {
        return outgoingNodes;
    }

    private void addNode(Node node) {
        if (node == null)
            throw new NullPointerException("node is null");

        if (owner != null || node.owner != null)
            throw new IllegalStateException("probably not the right stage to add a node");

        if (nodes.add(node) ^ node.coverers.add(this))
            throw new IllegalStateException("fuck up this shit");
    }

    void validateShallow() {
        if (!nodes.containsAll(outgoingNodes))
            throw new IllegalStateException("nodes !>= outgoingNodes");
        final ConnectedGraph myOwner = owner;
        for (Node n : nodes) {
            if (n.owner != myOwner)
                throw new IllegalStateException("owner mismatch");
            if (!n.coverers.contains(Clique.this))
                throw new IllegalStateException("clique cover fail");
        }
    }

    public void validate() {
        if (!nodes.containsAll(outgoingNodes))
            throw new IllegalStateException("nodes !>= outgoingNodes");
        final ConnectedGraph myOwner = owner;
        for (Node n : nodes) {
            if (n.owner != myOwner)
                throw new IllegalStateException("owner mismatch");
            n.validate();
        }
    }

    public void validateColours() {
        BitSet colourSet = new BitSet();
        for (Node n : nodes) {
            final int ci = n.colourIndex;
            if (ci == -1)
                throw new IllegalStateException("Colour missing");
            if (colourSet.get(ci))
                throw new IllegalStateException("Colour collision: " + ci);
            colourSet.set(ci);
        }
    }

    static Collection<Clique> createCliques(final ItemGroups groups, final Map<Item, Node> item2node) throws ConstraintException, ClassResolutionException {
        final Map<Object, Clique> group2cliques = new HashMap<>();
        for (Group group : groups.getGroups()) {
            if (group == null)
                throw new NullPointerException();
            group2cliques.put(group, new GroupRefClique(group));
        }
        FixedColourClique fcc = null;
        for (Map.Entry<Item, Node> pair : item2node.entrySet()) {
            final Item item = pair.getKey();
            final Node node = pair.getValue();

            final Group group = groups.getGroup(item);
            assert group != null;
            final Clique clique = group2cliques.get(group);
            assert clique != null;
            if (node.forcedColourIdentifier != null) {
                if (fcc == null) {
                    fcc = new FixedColourClique();
                    group2cliques.put(new Object(), fcc);
                }
                ((Clique) fcc).addNode(node);
                assert node.coverers.contains(fcc);
            }
            clique.addNode(node);

            try {
                if (DEBUG)
                    System.err.println("Clique: On item " + item.getOriginalIdentifier() + ":");
                item.forEachReference(new Reference.Visitor() {
                    @Override
                    public void visit(Reference<? extends Item> ref) throws Reference.Visitor.Stop {
                        if (DEBUG) {
                            System.err.println("Clique: \tVisiting reference " + ref.getClass().getName() + ", ident " +
                                    ref.getIdentifier() + ", strong=" + ref.isStrong());
                        }
                        assert ref.getSource() == item;
                        if (ref.isStrong()) {
                            try {
                                Item resolution = ref.getSource().getResolver().resolve(ref);
                                // If neither phantom reference nor self-reference
                                if (resolution != null && resolution != item) {
                                    Node target = item2node.get(resolution);
                                    if (target != null) // If that target is also in the request list
                                        clique.addNode(target);
                                }
                            } catch (ClassResolutionException e) {
                                throw new Reference.Visitor.Stop(e);
                            }
                        }
                    }
                });
            } catch (Reference.Visitor.Stop stop) {
                Throwable th = stop.getCause();
                if (th instanceof ClassResolutionException)
                    throw (ClassResolutionException) th;
                else
                    throw new RuntimeException("unexpected stop", stop);
            }
        }
        return group2cliques.values();
    }

}
