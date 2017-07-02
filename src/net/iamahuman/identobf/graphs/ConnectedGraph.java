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

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by luke1337 on 6/13/17.
 */
public class ConnectedGraph {
    Set<Clique> cliques = new HashSet<>();
    Set<Node> nodes = new HashSet<>();

    private ConnectedGraph() {
    }

    public interface Owned {
        ConnectedGraph getOwner();
    }

    public Collection<Clique> getCliques() {
        return cliques;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void validate() {
        for (Clique c : cliques) {
            if (c.owner != this)
                throw new IllegalStateException("owner mismatch");
            c.validateShallow();
        }
        for (Node n : nodes) {
            if (n.owner != this)
                throw new IllegalStateException("owner mismatch");
            if (!cliques.containsAll(n.coverers))
                throw new IllegalStateException("missed cliques exist");
            if (!nodes.containsAll(n.edges))
                throw new IllegalStateException("missed nodes exist");
            n.validateShallow();
        }
    }

    public int getMinChroma() {
        int max = 0;
        for (Clique c : cliques) {
            int size = c.nodes.size();
            if (max < size)
                max = size;
        }
        return max;
    }

    public int getMaxChroma() {
        return nodes.size();
    }

    private boolean addNode(Node other) {
        if (other.owner == this) {
            assert nodes.contains(other);
            return false;
        } else if (other.owner != null) {
            throw new IllegalArgumentException("node owned by a different graph");
        }

        other.owner = this;
        nodes.add(other);
        return true;
    }

    private boolean addClique(Clique other) {
        if (other.owner == this) {
            assert cliques.contains(other);
            return false;
        } else if (other.owner != null) {
            throw new IllegalArgumentException("clique owned by a different graph");
        }

        other.owner = this;
        cliques.add(other);
        return true;
    }

    public static Collection<ConnectedGraph> createFromWorklist(Collection<Node> worklist) {
        Collection<ConnectedGraph> graphs = new Vector<>();
        while (true) {
            Node root;
            {
                Iterator<Node> it = worklist.iterator();
                if (!it.hasNext()) return graphs;
                root = it.next();
                // this one removed later, so do not call it.remove() yet
            }
            if (root.owner != null)
                throw new IllegalStateException("roots in worklist must *not* have owner yet");
            ConnectedGraph graph = new ConnectedGraph();
            List<Iterator<? extends Owned>> stack = new ArrayList<>();
            stack.add(Collections.singleton(root).iterator());
            while (true) {
                final int last = stack.size() - 1;
                if (last == -1) break;
                Iterator<? extends Owned> top = stack.get(last);
                if (top.hasNext()) {
                    Owned child = top.next();
                    if (child instanceof Node) {
                        Node childNode = (Node) child;
                        if (graph.addNode(childNode)) {
                            if (!worklist.remove(childNode))
                                throw new IllegalStateException("not in worklist?");
                            stack.add(childNode.edges.iterator());
                            stack.add(childNode.coverers.iterator());
                        }
                    } else if (child instanceof Clique) {
                        Clique childClique = (Clique) child;
                        if (graph.addClique(childClique)) {
                            stack.add(childClique.nodes.iterator());
                        }
                    } else {
                        throw new IllegalStateException("invalid object");
                    }
                } else {
                    Object theTop = stack.remove(last);
                    assert top == theTop;
                }
            }
            graphs.add(graph);
        }
    }

}
