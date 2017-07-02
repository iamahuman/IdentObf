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
 * Created by luke1337 on 6/15/17.
 */
public class UnrecognizedClassException extends Reference.Visitor.Oops {
    public final AsmItem root;
    public final Object target;

    public UnrecognizedClassException(String s, AsmItem root, Object target, Throwable throwable) {
        super(s, throwable);
        this.root = root;
        this.target = target;
    }

    public UnrecognizedClassException(AsmItem root, Object target, Throwable throwable) {
        super(throwable);
        this.root = root;
        this.target = target;
    }

    public UnrecognizedClassException(String s, AsmItem root, Object target) {
        super(s);
        this.root = root;
        this.target = target;
    }

    public UnrecognizedClassException(AsmItem root, Object target) {
        super();
        this.root = root;
        this.target = target;
    }
}
