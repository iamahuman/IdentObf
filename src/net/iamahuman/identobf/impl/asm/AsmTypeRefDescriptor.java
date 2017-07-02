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
public abstract class AsmTypeRefDescriptor extends AsmDescriptor {
    protected static final int TYPE_UNKNOWN = 0;
    protected static final int TYPE_METHOD = 1;

    public AsmTypeRefDescriptor(AsmItem root, boolean isStrong) {
        super(root, isStrong);
    }

    protected abstract Type getTypeRef();
    protected abstract void setTypeRef(Type newType);

    @Override
    protected String getDesc() {
        Type typ = getTypeRef();
        return typ != null && typ.getSort() == Type.METHOD ? typ.getDescriptor() : null;
    }

    @Override
    protected void setDesc(String desc) {
        Type typ = getTypeRef();
        if (typ != null && typ.getSort() == Type.METHOD)
            setTypeRef(typ);
    }

    @Override
    protected void parse(DescriptorParser parser) throws DescriptorParser.ParseException {
        parser.parseRawMethodTypeSignature();
    }
}
