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

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.ItemResolver;
import net.iamahuman.identobf.nodes.Reference;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Created by luke1337 on 6/12/17.
 */
public final class MultiplexingItemResolver extends ItemResolver<Item> {

    private final ItemResolver<? extends Item>[] children;

    public MultiplexingItemResolver(ItemResolver<? extends Item>... children) {
        this.children = Arrays.copyOf(children, children.length);
    }

    @Override
    public Item resolve(String className) throws ClassResolutionException {
        for (ItemResolver<? extends Item> resolver : children) {
            Item item = resolver.resolve(className);
            if (item != null) {
                setResolver(item);
                return item;
            }
        }
        return null;
    }

    @Override
    public Item resolve(Reference<? extends Item> reference) throws ClassResolutionException {
        for (ItemResolver<? extends Item> resolver : children) {
            Item item = resolver.resolve(reference);
            if (item != null) {
                setResolver(item);
                return item;
            }
        }
        return null;
    }

    @Override
    public Collection<String> findClassesInPackage(final String pkgName) throws ClassResolutionException {
        Collection<String> result = new HashSet<>();
        for (ItemResolver<? extends Item> resolver : children) {
            result.addAll(resolver.findClassesInPackage(pkgName));
        }
        return result;
    }
}
