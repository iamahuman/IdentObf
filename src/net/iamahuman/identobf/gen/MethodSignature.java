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

import java.io.Serializable;

/**
 * Created by luke1337 on 6/19/17.
 */
public final class MethodSignature extends Signature implements Serializable {
    public final String name;
    public final String args;
    public final String ret;

    public MethodSignature(String name, String args, String ret) {
        super();
        if (name == null) throw new NullPointerException("name cannot be null");
        if (args == null) throw new NullPointerException("args cannot be null");
        if (ret == null) throw new NullPointerException("ret cannot be null");
        this.name = name;
        this.args = args;
        this.ret = ret;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ args.hashCode() ^ ret.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MethodSignature) {
            MethodSignature mo = (MethodSignature) other;
            return name.equals(mo.name) && args.equals(mo.args) && ret.equals(mo.ret);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "<" + name + ":(" + args + ")" + ret + ">";
    }

}
