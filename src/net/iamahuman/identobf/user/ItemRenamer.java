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

import net.iamahuman.identobf.nodes.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by luke1337 on 6/12/17.
 */
public class ItemRenamer {
    private final Collection<Item> itemsToModify;
    private final ItemGroups itemGroups;
    private final Map<? extends Item, ?> colourMap;
    private final Collection<Item> modifiedItems;
    private final Map<Group, Collection<Item>> references;

    public ItemRenamer(Collection<Item> itemsToModify, ItemGroups groups, Map<? extends Item, ?> colourMap) throws ClassResolutionException {
        this.itemsToModify = itemsToModify;
        this.itemGroups = groups;
        this.colourMap = colourMap;

        Collection<Item> modifiedItems = new HashSet<>();
        Map<Group, Collection<Item>> groupRefs = new HashMap<>();
        for (Item target : itemsToModify) {
            final Group tgroup = groups.getGroup(target);
            Collection<Item> refItems_ = groupRefs.get(tgroup);
            if (refItems_ == null) {
                groupRefs.put(tgroup, refItems_ = new HashSet<>());
            }
            final Collection<Item> refItems = refItems_;
            final boolean[] modBox = new boolean[]{false};
            try {
                target.forEachReference(new Reference.Visitor() {
                    @Override
                    public void visit(Reference<? extends Item> ref) throws Reference.Visitor.Stop {
                        Item resolution;
                        try {
                            resolution = ref.getSource().getResolver().resolve(ref);
                        } catch (ClassResolutionException e) {
                            throw new Reference.Visitor.Stop(e);
                        }
                        if (resolution != null) {
                            refItems.add(resolution);
                            Object newIdent = colourMap.get(resolution);
                            if (newIdent != null) {
                                ref.setIdentifier(newIdent);
                                modBox[0] = true;
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
            if (modBox[0])
                modifiedItems.add(target);
        }

        this.modifiedItems = modifiedItems;
        this.references = groupRefs;
    }

    public Collection<Item> getModifiedItems() {
        return modifiedItems;
    }

    public Map<Group, Collection<Item>> getReferences() {
        return references;
    }
}
