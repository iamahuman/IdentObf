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

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.Reference;
import net.iamahuman.identobf.nodes.RequestMeta;
import net.iamahuman.identobf.nodes.RequestMetaAdapter;
import net.iamahuman.identobf.user.RequestRegistry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by luke1337 on 6/16/17.
 */
public class AsmClassAccessRequestAdapter implements RequestMetaAdapter {

    private static int noPkgPrivate(int access) {
        if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0)
            access |= Opcodes.ACC_PUBLIC;
        return access;
    }

    private <T extends Reference<?>> void addNullableRef(Collection<? super T> refs, T ref) {
        if (ref.getIdentifier() != null) refs.add(ref);
    }

    private void noPkgPrivate(ClassNode node) {
        node.access = noPkgPrivate(node.access);
        for (MethodNode mn : node.methods) mn.access = noPkgPrivate(mn.access);
        for (FieldNode fn : node.fields) fn.access = noPkgPrivate(fn.access);
        for (InnerClassNode icn : node.innerClasses) icn.access = noPkgPrivate(icn.access);
    }

    @Override
    public RequestMeta getMeta(RequestRegistry registry, Item rawItem) {
        if (!(rawItem instanceof AsmItem))
            throw new UnsupportedOperationException(getClass().getName() + " only supports AsmItem");
        AsmItem item = (AsmItem) rawItem;

        ClassNode node = item.getResult();
        noPkgPrivate(node);

        Collection<Reference<AsmItem>> sgrefs = new HashSet<>();
        for (InnerClassNode icn : node.innerClasses) {
            addNullableRef(sgrefs, new AsmInnerClassReference(item, icn, AsmInnerClassReference.RT_INNER_NAME, false));
            addNullableRef(sgrefs, new AsmInnerClassReference(item, icn, AsmInnerClassReference.RT_OUTER_NAME, false));
        }
        addNullableRef(sgrefs, new AsmOuterClassReference(item, node, false));

        return new RequestMeta(rawItem, null, null, sgrefs,
                Collections.emptyList(), Collections.emptyList());
    }
}
