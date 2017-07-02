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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luke1337 on 6/12/17.
 */
public class ClasspathItemResolver<T extends Item, T_IDENT> extends ItemResolver<T> {
    protected final Classpath<? extends T, T_IDENT> classpath;
    protected final IdentityMapper<T_IDENT> mapper;
    protected final Map<T_IDENT, T> loadedItems = new HashMap<>();
    protected final Map<T_IDENT, ClassResolutionException> loadExceptions = new HashMap<>();

    public ClasspathItemResolver(Classpath<? extends T, T_IDENT> classpath, IdentityMapper<T_IDENT> mapper) {
        this.classpath = classpath;
        this.mapper = mapper;
    }

    @Override
    public T resolve(String className) throws ClassResolutionException {
        return resolveIdent(mapper.getIdentifier(className));
    }

    @Override
    public T resolve(Reference<? extends Item> reference) throws ClassResolutionException {
        // NOTE the type parameter of Reference is for the referrer, *not* the referee.
        return resolveIdent(mapper.getIdentifier(reference));
    }

    protected T resolveIdent(T_IDENT ident) throws ClassResolutionException {
        T item;
        if (loadedItems.containsKey(ident)) item = loadedItems.get(ident);
        else {
            ClassResolutionException exception = loadExceptions.get(ident);
            if (exception != null)
                throw exception;
            try {
                item = classpath.loadItem(ident);
            } catch (ClassResolutionException e) {
                loadExceptions.put(ident, e);
                throw e;
            }
            if (item != null)
                setResolver(item);
            loadedItems.put(ident, item);
        }
        return item;
    }

    @Override
    public Collection<String> findClassesInPackage(String pkgName) throws ClassResolutionException {
        return classpath.findClassesInPackage(pkgName);
    }
}
