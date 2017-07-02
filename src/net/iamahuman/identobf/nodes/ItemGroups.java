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

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.ConstraintException;
import net.iamahuman.identobf.user.GroupCollisionException;

import java.io.Serializable;
import java.util.*;

/**
 * Created by luke1337 on 6/12/17.
 */
public class ItemGroups {
    private final Collection<RequestMeta> requests;
    private final Map<Object, Group> ident2group;
    private final Map<Item, Group> item2group;
    private static final boolean DEBUG = true;

    public static final class AnonymousName implements Cloneable, Comparable<AnonymousName> {
        private static final long serialVersionUID = 1234502432411L;
        private int id;

        protected AnonymousName() { }

        public AnonymousName(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return "anon#" + id;
        }

        public int hashCode() {
            return id ^ 0xdacf01ae;
        }

        public boolean equals(Object other) {
            return other instanceof AnonymousName && id == ((AnonymousName) other).id;
        }

        @Override
        public int compareTo(AnonymousName other) {
            return id - other.id;
        }

        public AnonymousName clone() {
            return new AnonymousName(id);
        }
    }

    private <T> void doReplace(Map<T, Object> map, Map<Object, Object> repl) {
        for (Map.Entry<T, Object> entry : map.entrySet()) {
            Object ref = entry.getValue(), dest;
            while ((dest = repl.get(ref)) != null)
                ref = dest;
            entry.setValue(ref);
        }
    }

    public ItemGroups(Collection<RequestMeta> requests) throws ClassResolutionException, ConstraintException {
        super();

        Map<Object, Group> ident2group = new HashMap<>();

        Traverser<Group> traverser = new Traverser<Group>(requests) {
            int anonId = 0;

            @Override
            protected Object getIdentifier(RequestMeta request) {
                return request.groupIdentifier;
            }

            @Override
            protected Iterable<? extends Reference<? extends Item>> getReferences(RequestMeta request) {
                return request.sameGroupReferences;
            }

            @Override
            protected Group newGroup() {
                return new Group();
            }

            @Override
            protected void addItem(Group group, Item item) {
                group.items.add(item);
            }

            @Override
            protected void finalizeGroup(Group group, Object userIdent) {
                assert group.label == null;
                if (userIdent == null) userIdent = new AnonymousName(anonId++);
                group.label = userIdent;
                ident2group.put(userIdent, group);
            }
        };

        this.requests = requests;
        this.item2group = traverser.getItem2group();
        this.ident2group = ident2group;
    }

    public Collection<RequestMeta> getRequests() {
        return requests;
    }

    public Collection<Group> getGroups() {
        return ident2group.values();
    }

    public Group getGroup(Item item) {
        return item2group.get(item);
    }
}
