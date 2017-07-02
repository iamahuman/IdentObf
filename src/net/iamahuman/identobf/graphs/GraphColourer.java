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

/**
 * Created by luke1337 on 6/13/17.
 */
public abstract class GraphColourer {

    public final Graph graph;
    private static final boolean DEBUG = true;

    public GraphColourer(Graph graph) {
        super();
        this.graph = graph;
    }

    private interface Visitor {
        int onEntry(Node node);

        void onExit(Node node);

        boolean onSolution();
    }

    protected interface TrivialColourer {
        boolean doColour(Collection<Clique> cliques, int chroma);
    }

    public abstract void colour(GraphColourResultCallback callback);

    private static boolean searchSolution(Node[] workvector, int maxColour, Visitor visitor) {
        if (DEBUG)
            System.err.println("GraphColourer: searchSolution: start, wvlen=" + workvector.length);

        if (workvector.length == 0)
            return maxColour >= -1 && visitor.onSolution();
        else if (maxColour < 0)
            return false;

        for (int i = 0; i < workvector.length; i++) {
            workvector[i].colourIndex = -1;
            workvector[i].workvecIndex = i;
        }

        int top = 0;
        int maxtop = 0;
        boolean res = false;
        while (true) {
            Node node = workvector[top];
            int cidx = node.colourIndex;
            assert cidx <= maxColour;
            // Always advance by 1
            node.colourIndex = ++cidx;
            if (cidx <= maxColour) {
                // onEntry may or may not advance furthur
                int newidx = visitor.onEntry(node);
                if (newidx < cidx)
                    throw new IllegalStateException("onEntry effectively decremented colourIndex");
                cidx = newidx;
            }
            if (cidx <= maxColour) {
                node.colourIndex = cidx;
                if (top + 1 == workvector.length) {
                    if (visitor.onSolution()) {
                        res = true;
                        break;
                    }
                    for (int i = 0; i < workvector.length; i++) {
                        workvector[i].workvecIndex = i;
                    }
                } else {
                    top++;
                    if (maxtop < top)
                        maxtop = top;
                    assert workvector[top].colourIndex == -1;
                }
            } else {
                node.colourIndex = -1;
                if (--top == -1)
                    break;
                visitor.onExit(workvector[top]);
            }
        }


        if (!res) for (Node node : workvector) {
            node.colourIndex = -1;
            node.workvecIndex = -1;
        }

        if (DEBUG) System.err.println("GraphColourer: searchSolution: res=" + res + ", maxtop=" + maxtop);

        return res;
    }

    protected static int tryColourSubgraph(final ConnectedGraph graph, final TrivialColourer trivialColourer) {
        int min = graph.getMinChroma();
        int max = graph.getMaxChroma();
        if (min > max || max < 0)
            throw new IllegalStateException("wtf");
        if (DEBUG)
            System.err.println("Try coloring subgraph, mink=" + min + ", maxk=" + max);
        // If we do not start at min, tryColourCliqueAt may fail (at which we have put an assertion not to)
        // any chroma < min do not have any solutions anyway
        for (int i = min; i <= max; i++) {
            if (tryColourNontrivialAt(graph, i, trivialColourer))
                return i;
        }
        return -1;
    }

    private static boolean tryColourNontrivialAt(final ConnectedGraph graph, final int chroma, TrivialColourer colourer) {
        Node[] workvector;
        if (DEBUG)
            System.err.println("Subgraph colour, chroma #" + chroma);
        {
            List<Node> nodes = new ArrayList<>();
            for (Node node : graph.getNodes()) {
                if (!node.edges.isEmpty())
                    nodes.add(node);
                else if (node.coverers.size() > 1)
                    throw new IllegalStateException("wtf");
            }
            // NOTE *never* use Sorted"Set", since they smash up two elements with compare(a, b) = 0 into one
            nodes.sort(new Comparator<Node>() {
                @Override
                public int compare(Node t, Node t1) {
                    return t1.edges.size() - t.edges.size();
                }
            });
            workvector = nodes.toArray(new Node[0]);
        }
        final Visitor nontrivial = new Visitor() {
            @Override
            public int onEntry(Node node) {
                int myColour = node.colourIndex;
                final int myWork = node.workvecIndex;
                if (myColour == -1)
                    throw new IllegalStateException("colour is -1");
                // TODO cache elligible edges
                boolean mod;
                do {
                    mod = false;
                    for (Node target : node.edges) {
                        assert node != target;
                        final int yourColour = target.colourIndex;
                        if (yourColour != -1 && myColour == yourColour) {
                            mod = true;
                            myColour = yourColour + 1;
                            if (myColour >= chroma)
                                break;
                        }
                    }
                } while (mod);
                return myColour;
            }

            @Override
            public void onExit(Node node) {
            }

            @Override
            public boolean onSolution() {
                if (DEBUG)
                    System.err.println("Enter trivial colouring, chroma #" + chroma);
                if (GraphColourer.class.desiredAssertionStatus()) {
                    for (Node n : workvector) {
                        if (n.colourIndex == -1)
                            throw new AssertionError("missed nodes");
                    }
                    for (Node n : workvector) {
                        for (Node e : n.getEdges()) {
                            if (n.colourIndex != -1 && n.colourIndex == e.colourIndex)
                                throw new AssertionError("invariant check failed");
                        }
                    }
                }
                return colourer.doColour(graph.cliques, chroma);
            }
        };
        return searchSolution(workvector, chroma - 1, nontrivial);
    }

    protected static boolean tryColourCliqueAt(final Clique c, final int chroma) {
        Node[] workvector;
        final BitSet usedColours = new BitSet(chroma);
        {
            Collection<Node> workset = new ArrayList<>(c.nodes.size() / 4 * 3);
            for (Node n : c.nodes) {
                if (n.colourIndex != -1)
                    usedColours.set(n.colourIndex);
                else if (n.isTrivial())
                    workset.add(n);
                else
                    throw new IllegalStateException("neither?! (edge: " + n.edges.size() + ", cv: " + n.coverers.size() + ")" + ", wv: " + n.workvecIndex);
            }
            workvector = workset.toArray(new Node[0]);
        }
        Visitor visitor = new Visitor() {
            @Override
            public int onEntry(Node node) {
                int i = usedColours.nextClearBit(node.colourIndex);
                if (i < chroma) usedColours.set(i);
                return i;
            }

            @Override
            public void onExit(Node node) {
                usedColours.clear(node.colourIndex);
            }

            @Override
            public boolean onSolution() {
                return true;
            }
        };
        return searchSolution(workvector, chroma - 1, visitor);
    }

}
