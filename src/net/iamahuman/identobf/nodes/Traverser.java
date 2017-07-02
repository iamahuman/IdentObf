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

package net.iamahuman.identobf.nodes;

import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.ConstraintException;
import net.iamahuman.identobf.user.GroupCollisionException;

import java.util.*;

/**
 * Created by luke1337 on 6/16/17.
 */
public abstract class Traverser<T> {

    protected abstract Object getIdentifier(RequestMeta request);
    protected abstract Iterable<? extends Reference<? extends Item>> getReferences(RequestMeta request);
    protected abstract T newGroup();
    protected abstract void addItem(T group, Item item);
    protected abstract void finalizeGroup(T group, Object userIdent);

    private Map<Item, T> item2group;

    public Traverser(Iterable<? extends RequestMeta> requests) throws ConstraintException, ClassResolutionException {
        Map<Item, Collection<Item>> edges = new HashMap<>();
        Map<Item, Object> item2ident = new HashMap<>();

        for (RequestMeta meta : requests) {
            Collection<Item> out = edges.get(meta.item);
            if (out == null) edges.put(meta.item, out = new HashSet<>());
            item2ident.put(meta.item, getIdentifier(meta));
            for (Reference<? extends Item> sgRef : getReferences(meta)) {
                Item resolution = sgRef.getSource().getResolver().resolve(sgRef);
                if (resolution == null) {
                    if (sgRef.isStrong())
                        throw new ConstraintException("sgref resolution failed");
                } else if (!resolution.equals(meta.item)) {
                    Collection<Item> otherOut = edges.get(resolution);
                    if (otherOut == null)
                        edges.put(resolution, otherOut = new HashSet<>());
                    out.add(resolution);
                    otherOut.add(meta.item);
                }
            }
        }

        Map<Object, T> ident2group = new HashMap<>();
        Map<Item, T> item2group = new HashMap<>();

        for (Item item : item2ident.keySet()) {
            if (item2group.containsKey(item))
                continue;

            T group = newGroup();
            addItem(group, item);
            item2group.put(item, group);

            Object ident = item2ident.get(item);

            List<Iterator<? extends Item>> stack = new ArrayList<>();
            stack.add(edges.get(item).iterator());
            int top;
            while ((top = stack.size()) > 0) {
                Iterator<? extends Item> iterator = stack.get(top - 1);
                boolean doNext;
                while (doNext = iterator.hasNext()) {
                    Item child = iterator.next();
                    if (!item2group.containsKey(child)) {
                        // Same group id?
                        Object otherIdent = item2ident.get(child);
                        if (otherIdent != null) {
                            if (ident == null) ident = otherIdent;
                            else if (!ident.equals(otherIdent))
                                throw new GroupCollisionException("group id collision: " + ident + ", " + otherIdent);
                        }
                        item2group.put(child, group);
                        addItem(group, child);
                        stack.add(edges.get(child).iterator());
                        break;
                    }
                }
                if (!doNext) {
                    Object theTop = stack.remove(top - 1);
                    assert theTop == iterator;
                }
            }

            finalizeGroup(group, ident);
        }

        this.item2group = item2group;
    }

    public Map<Item, T> getItem2group() {
        return item2group;
    }
}
