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

import java.util.Collection;

/**
 * Created by luke1337 on 6/13/17.
 */
public class SimpleGraphColourer extends GraphColourer {

    public SimpleGraphColourer(Graph graph) {
        super(graph);
    }

    @Override
    public void colour(GraphColourResultCallback callback) {
        for (ConnectedGraph subgraph : graph.getSubgraphs()) {
            int chroma = tryColourSubgraph(subgraph, new TrivialColourer() {
                @Override
                public boolean doColour(Collection<Clique> cliques, int chroma) {
                    for (Clique c : cliques) {
                        if (!tryColourCliqueAt(c, chroma))
                            throw new IllegalStateException("colouring clique failed - should never happen");
                    }
                    return true;
                }
            });
            if (chroma >= 0) callback.onColour(subgraph.nodes, chroma);
            else {
                callback.onUnsatisfied();
                return;
            }
        }
        callback.onComplete();
    }
}
