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

import org.objectweb.asm.tree.InnerClassNode;

/**
 * Created by luke1337 on 6/14/17.
 */
public class AsmInnerClassReference extends AsmReference {

    private static final boolean DEBUG = false;

    public final int reftype;
    public final InnerClassNode innerClassNode;

    public static final int RT_INNER_NAME = 0;
    public static final int RT_OUTER_NAME = 1;

    public AsmInnerClassReference(AsmItem source, InnerClassNode node, int reftype, boolean isStrong) {
        super(source, isStrong);
        this.innerClassNode = node;
        this.reftype = reftype;
        if (DEBUG) System.err.println("<AsmInnerClassReference> node=" + node + ", reftype=" + reftype);
    }

    @Override
    public Object getIdentifier() {
        switch (reftype) {
            case RT_INNER_NAME:
                return innerClassNode.name;
            case RT_OUTER_NAME:
                return innerClassNode.outerName;
            default:
                throw new IllegalStateException("invalid reftype value");
        }
    }

    @Override
    public void setIdentifier(Object newIdentifier) {
        switch (reftype) {
            case RT_INNER_NAME:
                innerClassNode.name = (String) newIdentifier;
                if (source.config.renameInnerClassSimpleName && innerClassNode.innerName != null) {
                    String[] k = ((String) newIdentifier).split("/");
                    innerClassNode.innerName = k[k.length - 1];
                }
                break;
            case RT_OUTER_NAME:
                innerClassNode.outerName = (String) newIdentifier;
                break;
            default:
                throw new IllegalStateException("invalid reftype value");
        }
    }
}
