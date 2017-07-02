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

package net.iamahuman.identobf.user;

/**
 * Created by luke1337 on 6/15/17.
 */
public class GroupCollisionException extends ConstraintException {
    public GroupCollisionException() {
    }

    public GroupCollisionException(String s) {
        super(s);
    }

    public GroupCollisionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public GroupCollisionException(Throwable throwable) {
        super(throwable);
    }
}
