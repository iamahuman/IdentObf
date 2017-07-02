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

package net.iamahuman.identobf.impl.asm;

import org.objectweb.asm.Type;

/**
 * Created by luke1337 on 6/16/17.
 */
public abstract class AsmTypeRefReference extends AsmReference {

    protected AsmTypeRefReference(AsmItem source, boolean isStrong) {
        super(source, isStrong);
    }

    protected abstract Type getTypeRef();

    protected abstract void setTypeRef(Type newType);

    @Override
    public Object getIdentifier() {
        Type typ = getTypeRef();
        if (typ == null) return null;
        switch (typ.getSort()) {
            case Type.OBJECT:
                return typ.getInternalName();
            case Type.ARRAY:
                int i = 0;
                final String nam = typ.getInternalName();
                while (nam.charAt(i) == '[') i++;
                if (nam.charAt(i) == 'L' && nam.endsWith(";"))
                    return nam.substring(i + 1, nam.length() - 1);
                break;
        }
        return null;
    }

    @Override
    public void setIdentifier(Object newIdentifier) {
        Type typ = getTypeRef();
        switch (typ.getSort()) {
            case Type.OBJECT:
                setTypeRef(Type.getType('L' + (String) newIdentifier + ';'));
                break;
            case Type.ARRAY:
                int dim = 0;
                final String nam = typ.getInternalName();
                while (nam.charAt(dim) == '[') dim++;
                if (nam.charAt(dim) == 'L' && nam.endsWith(";")) {
                    setTypeRef(Type.getType(nam.substring(0, dim + 1) + nam + ";"));
                }
                break;
        }
    }
}
