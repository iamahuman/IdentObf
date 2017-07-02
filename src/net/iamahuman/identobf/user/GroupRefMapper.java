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

package net.iamahuman.identobf.user;

import net.iamahuman.identobf.graphs.Clique;
import net.iamahuman.identobf.graphs.GroupRefClique;
import net.iamahuman.identobf.graphs.Node;
import net.iamahuman.identobf.nodes.Group;
import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.ItemGroups;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by luke1337 on 6/15/17.
 */
public class GroupRefMapper {

    private final Map<Group, Collection<Item>> groupRefMap;

    public GroupRefMapper(Collection<? extends Clique> cliques) {
        super();
        Map<Group, Collection<Item>> groupRefMap = new HashMap<>();
        for (Clique c : cliques) {
            if (c instanceof GroupRefClique) {
                for (Group g : ((GroupRefClique) c).getGroups()) {
                    Collection<Item> items = groupRefMap.get(g);
                    if (items == null)
                        groupRefMap.put(g, items = new HashSet<>()); // must be a set
                    for (Node n : c.getNodes())
                        items.addAll(n.getItems());
                }
            }
        }
        this.groupRefMap = groupRefMap;
    }

    public Map<Group, Collection<Item>> getGroupRefMap() {
        return groupRefMap;
    }
}
