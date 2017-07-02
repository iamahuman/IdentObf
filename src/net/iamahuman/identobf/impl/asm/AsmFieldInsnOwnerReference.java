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

import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Created by luke1337 on 6/15/17.
 */
public class AsmFieldInsnOwnerReference extends AsmReference {
    final FieldInsnNode insn;

    public AsmFieldInsnOwnerReference(AsmItem source, FieldInsnNode insn, boolean isStrong) {
        super(source, isStrong);
        this.insn = insn;
    }

    @Override
    public Object getIdentifier() {
        String owner = insn.owner;
        if (owner == null) throw new NullPointerException();
        return owner;
    }

    @Override
    public void setIdentifier(Object newIdentifier) {
        insn.owner = (String) newIdentifier;
    }
}
