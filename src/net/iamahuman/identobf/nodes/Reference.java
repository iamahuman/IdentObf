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

package net.iamahuman.identobf.nodes;

/**
 * Created by luke1337 on 6/12/17.
 */
public interface Reference<T extends Item> {
    interface Visitor {
        class Stop extends Exception {
            public Stop() {
            }

            public Stop(String s) {
                super(s);
            }

            public Stop(String s, Throwable throwable) {
                super(s, throwable);
            }

            public Stop(Throwable throwable) {
                super(throwable);
            }

        }

        class Oops extends Stop {
            public Oops() {
            }

            public Oops(String s) {
                super(s);
            }

            public Oops(String s, Throwable throwable) {
                super(s, throwable);
            }

            public Oops(Throwable throwable) {
                super(throwable);
            }
        }

        void visit(Reference<? extends Item> reference) throws Stop;
    }

    T getSource();

    Object getIdentifier();

    void setIdentifier(Object newIdentifier);

    boolean isStrong();
}
