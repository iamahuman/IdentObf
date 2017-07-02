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

import net.iamahuman.identobf.nodes.Reference;

/**
 * Created by luke1337 on 6/14/17.
 */
public abstract class AsmReference implements Reference<AsmItem> {

    protected final AsmItem source;
    protected final boolean isStrong;

    AsmReference(AsmItem source, boolean isStrong) {
        super();
        this.source = source;
        this.isStrong = isStrong;
    }

    @Override
    public AsmItem getSource() {
        return source;
    }

    @Override
    public final boolean isStrong() {
        return isStrong;
    }

    @Override
    public String toString() {
        return "<AsmReference" +
                (isStrong ? "+ " : "? ") +
                getIdentifier() +
                " <- " +
                source + ">";
    }
}
