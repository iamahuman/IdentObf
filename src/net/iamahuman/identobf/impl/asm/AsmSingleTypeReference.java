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

/**
 * Created by luke1337 on 6/15/17.
 */
public abstract class AsmSingleTypeReference extends AsmReference {
    protected final int reftype;

    /** Full reference (e.g. <code>Ljava/lang/String;</code> or <code>[Ljava/lang/Object;</code> or <code>I</code>) **/
    public static final int REF_FULL = 0;

    /** Reference only (e.g. <code>java/lang/String</code> or <code>[Ljava/lang/Object;</code>) **/
    public static final int REF_REFONLY = 1;

    /** Internal name only (e.g. <code>java/lang/String</code>) **/
    public static final int REF_CLASSONLY = 2;

    protected AsmSingleTypeReference(AsmItem source, int reftype, boolean isStrong) {
        super(source, isStrong);
        this.reftype = reftype;
        if (reftype != REF_FULL && reftype != REF_REFONLY && reftype != REF_CLASSONLY)
            throw new IllegalArgumentException("Invalid reftype");
    }

    protected abstract String getDesc();

    protected abstract void setDesc(String newDesc);

    @Override
    public Object getIdentifier() {
        final String desc = getDesc();
        if (reftype == REF_CLASSONLY || desc == null)
            return desc;
        int i = 0;
        while (desc.charAt(i) == '[') i++;
        if (reftype != REF_FULL && i == 0) {
            return desc;
        } else {
            if (desc.charAt(i) == 'L' && desc.endsWith(";"))
                return desc.substring(i + 1, desc.length() - 1);
        }
        return null;
    }

    @Override
    public void setIdentifier(Object newIdentifier) {
        if (reftype == REF_CLASSONLY) {
            setDesc((String) newIdentifier);
            return;
        }
        final String desc = getDesc();
        if (desc == null) throw new UnsupportedOperationException("cannot set referee for this reference");
        int i = 0;
        while (desc.charAt(i) == '[') i++;
        if (reftype != REF_FULL && i == 0) {
            setDesc((String) newIdentifier);
        } else {
            if (desc.charAt(i) == 'L' && desc.endsWith(";"))
                setDesc(desc.substring(0, i + 1) + newIdentifier + ";");
        }
    }
}
