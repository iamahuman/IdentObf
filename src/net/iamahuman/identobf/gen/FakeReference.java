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

import net.iamahuman.identobf.nodes.Reference;

/**
 * Created by luke1337 on 6/19/17.
 */
public class FakeReference implements Reference<FakeItem> {

    private final FakeItem source, destination;

    FakeReference(FakeItem source, FakeItem destination) {
        super();
        this.source = source;
        this.destination = destination;
    }

    @Override
    public FakeItem getSource() {
        return source;
    }

    public FakeItem getDestination() {
        return destination;
    }

    @Override
    public Object getIdentifier() {
        return null;
    }

    @Override
    public void setIdentifier(Object newIdentifier) {

    }

    @Override
    public boolean isStrong() {
        return true;
    }
}
