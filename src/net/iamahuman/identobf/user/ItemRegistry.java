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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by luke1337 on 6/12/17.
 */
public class ItemRegistry {
    protected final Collection<Item> items = new HashSet<>();
    private static final boolean DEBUG = false;
    protected final ItemResolver<? extends Item> resolver;

    public ItemRegistry(ItemResolver<? extends Item> resolver) {
        super();
        this.resolver = resolver;
    }

    public void requestClass(String className) throws ClassResolutionException {
        Item item;
        try {
            item = resolver.resolve(className);
            if (item == null)
                throw new ClassResolutionException("class not found");
        } catch (ClassResolutionException e) {
            if (DEBUG)
                e.printStackTrace();
            throw e;
        }
        addItem(item);
    }

    public void requestClass(Reference<? extends Item> reference) throws ClassResolutionException {
        Item item;
        try {
            item = resolver.resolve(reference);
            if (item == null)
                throw new ClassResolutionException("class not found");
        } catch (ClassResolutionException e) {
            if (DEBUG)
                e.printStackTrace();
            throw e;
        }
        addItem(item);
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public void addItems(Collection<Item> items) {
        this.items.addAll(items);
    }

    public Collection<Item> getItems() {
        return items;
    }

    public ItemResolver<? extends Item> getItemResolver() {
        return resolver;
    }

    public void requestAllClasses() throws ClassResolutionException {
        requestPackage(null);
    }

    private void collectItemsInPackage(Collection<Item> itemsOut, String pkgName) throws ClassResolutionException {
        for (String clsName : resolver.findClassesInPackage(pkgName)) {
            Item item = resolver.resolve(clsName);
            if (item == null) throw new NullPointerException();
            itemsOut.add(item);
        }
    }

    public void requestPackage(String pkgName) throws ClassResolutionException {
        Collection<Item> items = new ArrayList<>();
        collectItemsInPackage(items, pkgName);
        addItems(items);
    }

    public void requestPackages(String... packages) throws ClassResolutionException {
        Collection<Item> items = new ArrayList<>();
        for (String pkgName : packages)
            collectItemsInPackage(items, pkgName);
        addItems(items);
    }

    public void requestPackages(Iterable<String> packages) throws ClassResolutionException {
        Collection<Item> items = new ArrayList<>();
        for (String pkgName : packages)
            collectItemsInPackage(items, pkgName);
        addItems(items);
    }

    public void requestClasses(String... classNames) throws ClassResolutionException {
        Collection<Item> items = new ArrayList<>();
        for (String className : classNames) {
            Item item = resolver.resolve(className);
            if (item == null) throw new ClassResolutionException("class not found");
            items.add(item);
        }
        addItems(items);
    }

    public void requestClasses(Iterable<String> classNames) throws ClassResolutionException {
        Collection<Item> items = new ArrayList<>();
        for (String className : classNames) {
            Item item = resolver.resolve(className);
            if (item == null) throw new ClassResolutionException("class not found");
            items.add(item);
        }
        addItems(items);
    }
}