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
import net.iamahuman.identobf.nodes.ItemResolver;
import net.iamahuman.identobf.nodes.Reference;
import net.iamahuman.identobf.user.ClassResolutionException;

import java.util.Collection;

/**
 * Created by luke1337 on 6/19/17.
 */
public class FakeItemResolver extends ItemResolver<FakeItem> {



    @Override
    public FakeItem resolve(String className) throws ClassResolutionException {
        return null;
    }

    @Override
    public FakeItem resolve(Reference<? extends Item> reference) throws ClassResolutionException {
        if (reference instanceof FakeReference)
            return ((FakeReference) reference).getDestination();
        return null;
    }

    @Override
    public Collection<String> findClassesInPackage(String pkgName) throws ClassResolutionException {
        return null;
    }
}
