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

import org.objectweb.asm.tree.FieldNode;

/**
 * Created by luke1337 on 6/15/17.
 */
public class AsmFieldReference extends AsmSingleTypeReference {
    public final FieldNode insn;

    public AsmFieldReference(AsmItem item, FieldNode insn, boolean isStrong) {
        super(item, REF_FULL, isStrong);
        this.insn = insn;
    }

    @Override
    protected String getDesc() {
        String desc = insn.desc;
        if (desc == null) throw new NullPointerException();
        return desc;
    }

    @Override
    protected void setDesc(String newDesc) {
        insn.desc = newDesc;
    }
}
