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

package net.iamahuman.identobf.gen;

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.Reference;

import java.util.*;

/**
 * Created by luke1337 on 6/19/17.
 */
public class FakeItem extends Item {
    public final Map<MethodSignature, Collection<Signature>> methods = new HashMap<>();
    public final Set<FieldSignature> fields = new HashSet<>();
    public final Set<FakeItem> edges = new HashSet<>();

    public FakeItem() {
        super();
    }

    @Override
    public void forEachReference(Reference.Visitor visitor) throws Reference.Visitor.Stop {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getOriginalIdentifier() {
        return null;
    }


}
