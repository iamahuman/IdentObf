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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by luke1337 on 6/19/17.
 */
public class ClassLinkSignature implements Serializable {
    public final Set<MethodSignature> methods;
    public final Set<FieldSignature> fields;

    public ClassLinkSignature(Set<MethodSignature> methods, Set<FieldSignature> fields) {
        this.methods = Collections.unmodifiableSet(methods);
        this.fields = Collections.unmodifiableSet(fields);
    }

    @Override
    public int hashCode() {
        return methods.hashCode() ^ fields.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ClassLinkSignature) {
            ClassLinkSignature co = (ClassLinkSignature) other;
            return methods.equals(co.methods) && fields.equals(co.fields);
        } else {
            return false;
        }
    }

    public boolean isSubsetOf(ClassLinkSignature other) {
        return methods.containsAll(other.methods) && fields.containsAll(other.fields);
    }
}
