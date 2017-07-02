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
import net.iamahuman.identobf.nodes.*;
import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.ConstraintException;

import java.util.*;

/**
 * Created by luke1337 on 6/13/17.
 */
public class Graph {

    private final Collection<ConnectedGraph> subgraphs;
    private static final boolean DEBUG = true;
    ItemGroups groups;
    Collection<Clique> cliques;
    Map<Item, Node> item2node;

    public Graph(Collection<RequestMeta> metas) throws ClassResolutionException, ConstraintException {
        ItemGroups groups = new ItemGroups(metas);

        Set<Node> nodes = new HashSet<>();

        Traverser<Node> traverser = new Traverser<Node>(metas) {
            @Override
            protected Object getIdentifier(RequestMeta request) {
                return request.fixedColour;
            }

            @Override
            protected Iterable<? extends Reference<? extends Item>> getReferences(RequestMeta request) {
                return request.sameColourReferences;
            }

            @Override
            protected Node newGroup() {
                return new Node();
            }

            @Override
            protected void addItem(Node group, Item item) {
                group.items.add(item);
            }

            @Override
            protected void finalizeGroup(Node group, Object userIdent) {
                group.forcedColourIdentifier = userIdent;
                nodes.add(group);
            }
        };

        Map<Item, Node> item2node = traverser.getItem2group();
        Collection<Clique> cliques = Clique.createCliques(groups, item2node);
        if (DEBUG)
            System.err.println("Graph: progress so far: " + nodes.size() + " nodes, " + cliques.size() + " cliques");

        generatePrimaryEdges(item2node, metas);
        populateCliqueReverse(nodes);
        generateSecondaryEdges(cliques);

        this.item2node = item2node;
        this.groups = groups;
        this.cliques = cliques;
        this.subgraphs = ConnectedGraph.createFromWorklist(nodes);

        if (!nodes.isEmpty())
            throw new IllegalStateException("worklist has not been emptied");
    }

    private static void generatePrimaryEdges(Map<Item, Node> item2node, Collection<RequestMeta> metas) throws ConstraintException, ClassResolutionException {
        for (RequestMeta req : metas) {
            Node node = item2node.get(req.item);

            for (Reference<? extends Item> ref : req.differentColourReferences) {
                Item resolution = ref.getSource().getResolver().resolve(ref);
                Node other = resolution == null ? null : item2node.get(resolution);

                if (other == null) {
                    if (ref.isStrong())
                        throw new ConstraintException("reference to external or nonexistent item: different colour references");
                    else
                        continue;
                }
                if (node.equals(other))
                    throw new ConstraintException("self-contradiction on request meta: different colour references");

                HashSet<Clique> intersection = new HashSet<>(node.coverers);
                intersection.retainAll(other.coverers);
                if (intersection.isEmpty()) // Optimization
                    node.connect(other);
            }
        }
    }

    private static void populateCliqueReverse(Collection<Node> nodes) {
        for (Node node : nodes) {
            if (!node.isTrivial()) {
                for (Clique c : node.coverers) {
                    c.outgoingNodes.add(node);
                }
            }
        }
    }

    private static void generateSecondaryEdges(Collection<Clique> cliques) throws ConstraintException {
        for (Clique clique : cliques) {
            // XXX use commons?
            Node[] vertexes = clique.outgoingNodes.toArray(new Node[0]);
            int i, j;
            for (i = 1; i < vertexes.length; i++) {
                Node left = vertexes[i];
                for (j = 0; j < i; j++) {
                    Node right = vertexes[j];
                    left.connect(right);
                }
            }
        }
    }

    public ItemGroups getGroups() {
        return groups;
    }

    public Collection<Item> getItems() {
        return item2node.keySet();
    }

    public Collection<ConnectedGraph> getSubgraphs() {
        return subgraphs;
    }

    public Collection<Clique> getCliques() {
        return cliques;
    }

    public Node getNode(Item item) {
        return item2node.get(item);
    }
}
