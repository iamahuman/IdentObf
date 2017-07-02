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

/**
 * Created by luke1337 on 6/15/17.
 */
public class ReferenceProxy<T extends Item> implements Reference<T> {

    protected T source;
    protected String identifier;
    protected boolean isStrong;
    protected Reference<? extends T> inner;

    public ReferenceProxy(T source, String identifier, boolean isStrong) {
        super();
        this.source = source;
        this.identifier = identifier;
        this.isStrong = isStrong;
    }

    public ReferenceProxy(Reference<? extends T> inner) {
        super();
        this.isStrong = inner.isStrong();

        // Some optimizations
        // NOTE DO NOT USE instanceof since subclasses may behave differently.
        while (inner.getClass() == ReferenceProxy.class) {
            //noinspection unchecked
            inner = ((ReferenceProxy<? extends T>) inner).inner;
        }
        this.inner = inner;
    }

    public ReferenceProxy(Reference<? extends T> inner, boolean isStrong) {
        super();
        this.inner = inner;
        this.isStrong = isStrong;
    }

    @Override
    public T getSource() {
        return inner == null ? source : inner.getSource();
    }

    @Override
    public Object getIdentifier() {
        return inner == null ? identifier : inner.getIdentifier();
    }

    @Override
    public void setIdentifier(Object newIdentifier) {
        if (inner == null)
            throw new UnsupportedOperationException("This is a null proxy");
        inner.setIdentifier(newIdentifier);
    }

    @Override
    public boolean isStrong() {
        return isStrong;
    }
}
