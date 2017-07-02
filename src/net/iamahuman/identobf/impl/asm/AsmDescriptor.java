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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke1337 on 6/14/17.
 */
public abstract class AsmDescriptor {
    final AsmItem root;
    final boolean isStrong;
    final List<String> lexemes = new ArrayList<>();
    final List<Reference> refs = new ArrayList<>();
    boolean dirty = false;

    public class Reference extends AsmReference {

        final int index;

        Reference(int index) {
            super(root, AsmDescriptor.this.isStrong);
            this.index = index;
        }

        @Override
        public Object getIdentifier() {
            return lexemes.get(index);
        }

        @Override
        public void setIdentifier(Object newIdentifier) {
            if (!dirty) {
                root.dirtyDescs.add(AsmDescriptor.this);
                dirty = true;
            }
            lexemes.set(index, (String) newIdentifier);
        }

    }

    protected abstract String getDesc();
    protected abstract void setDesc(String desc);
    protected abstract void parse(DescriptorParser parser) throws DescriptorParser.ParseException;

    public AsmDescriptor(AsmItem root, boolean isStrong) {
        super();
        this.root = root;
        this.isStrong = isStrong;
    }

    void sync() {
        if (dirty) {
            StringBuilder newSig = new StringBuilder();
            for (String lexeme : lexemes) {
                newSig.append(lexeme);
            }
            setDesc(newSig.toString());
            // This should be the last statement just before the end of conditional
            dirty = false;
        }
    }

    void accept(net.iamahuman.identobf.nodes.Reference.Visitor visitor) throws net.iamahuman.identobf.nodes.Reference.Visitor.Stop {
        for (Reference ref : refs) {
            visitor.visit(ref);
        }
    }

    void parseAndAccept(net.iamahuman.identobf.nodes.Reference.Visitor visitor) throws net.iamahuman.identobf.nodes.Reference.Visitor.Stop {
        final String desc = getDesc();
        if (desc == null) return;
        DescriptorParser parser = new DescriptorParser(lexemes, desc, new DescriptorParser.Collector() {
                @Override
                public void onIdent(int index) {
                    refs.add(new Reference(index));
                }
            });
        try {
            parse(parser);
            accept(visitor);
        } catch (DescriptorParser.ParseException e) {
            if (root.config.strict)
                throw new UnrecognizedClassException("Parse desc failed @ " + parser.ptr + ": " + desc, root, this, e);
        }
    }

}
