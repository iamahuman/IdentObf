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
 * <p>
 * NOTE: super naive lexer + LL(1) parser mixed in one code
 */
public class DescriptorParser {
    final Collector collector;
    final List<String> lexemes;
    final String desc;
    int ptr = 0, lastSplit = 0;

    public static class ParseException extends Exception {

        public ParseException() {
        }

        public ParseException(String s) {
            super(s);
        }

        public ParseException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public ParseException(Throwable throwable) {
            super(throwable);
        }

    }

    public interface Collector {
        void onIdent(int index);
    }

    public DescriptorParser(List<String> lexemes, String desc, Collector collector) {
        super();
        this.lexemes = lexemes;
        this.desc = desc;
        this.collector = collector;
    }

    protected char peek() throws ParseException {
        final int i = ptr;
        if (i < desc.length())
            return desc.charAt(i);
        else
            throw new ParseException("EOF");
    }

    protected char peekOpt(char def) {
        final int i = ptr;
        return i < desc.length() ? desc.charAt(i) : def;
    }

    protected char consume() throws ParseException {
        final int i = ptr;
        if (i < desc.length()) {
            char c = desc.charAt(i);
            ptr = i + 1;
            return c;
        } else throw new ParseException("EOF");
    }

    protected void expectEOF() throws ParseException {
        if (ptr < desc.length())
            throw new ParseException("expected EOF");
        split();
    }

    protected void expect(char c) throws ParseException {
        char k = consume();
        if (c != k)
            throw new ParseException("expected " + c + ", got " + k);
    }

    protected void unpeek(char c) {
        boolean ok = false;
        try {
            ok = (c == consume());
        } catch (ParseException ignored) {
        }
        if (!ok)
            throw new IllegalStateException("peek out of sync");
    }

    protected void split() {
        final int start = lastSplit, end = ptr;
        if (start < end) {
            lexemes.add(desc.substring(lastSplit, end));
            lastSplit = end;
        }
    }

    protected static final int IDENT_WITHPACKAGE = 1,
                                IDENT_THEN_TYPE_ARG_OPT = 2,
                                IDENT_THEN_COLON = 4;

    protected int identifier(int flags) throws ParseException {
        split();
        final CharSequence desc = this.desc;
        char termCh = ((flags & IDENT_THEN_COLON) == IDENT_THEN_COLON) ? ':' : ';';
        char ch;
        int i = ptr, s = desc.length();
        for (; i < s; i++) {
            int act = 0;
            ch = desc.charAt(i);
            switch (ch) {
                case '<':
                    if ((flags & IDENT_THEN_TYPE_ARG_OPT) == IDENT_THEN_TYPE_ARG_OPT)
                        act = 2;
                    break;
                case ':':case ';':
                    if (ch == termCh)
                        act = 2;
                    break;
                case '/':
                    if ((flags & IDENT_WITHPACKAGE) == IDENT_WITHPACKAGE)
                        act = 1;
                    break;
                case '.':case '[':case '>':
                    break;
                default:
                    act = 1;
            }
            switch (act) {
                case 0:
                    throw new ParseException("unexpected " + ch + " @ " + i + ", start " + ptr);
                case 2:
                    ptr = i;
                    split();
                    return lexemes.size() - 1;
            }
        }
        throw new ParseException("unexpected EOF");
    }

    public static final int TYPE_VOID = 1,
                            TYPE_BASE = 2,
                            TYPE_RAWCLASS = 4,
                            TYPE_RAWARRAY = 8,
                            TYPE_TYPEVAR = 16,
                            TYPE_TYPEPFX = 32,
                            TYPE_CLASS = TYPE_RAWCLASS | 64,
                            TYPE_ARRAY = TYPE_RAWARRAY | 128,
                            TYPE_FIELDTYPE = TYPE_CLASS | TYPE_ARRAY | TYPE_TYPEVAR,
                            TYPE_TYPEARG = TYPE_FIELDTYPE | TYPE_TYPEPFX,
                            TYPE_RAWFIELDTYPE = TYPE_RAWCLASS | TYPE_RAWARRAY,
                            TYPE_NORMAL = TYPE_BASE | TYPE_FIELDTYPE,
                            TYPE_RAWNORMAL = TYPE_BASE | TYPE_RAWFIELDTYPE,
                            TYPE_RETURNTYPE = TYPE_NORMAL | TYPE_VOID,
                            TYPE_RAWRETURNTYPE = TYPE_RAWNORMAL | TYPE_VOID;

    public void parseTypeSignature(int flags) throws ParseException {
        char c = consume();
        boolean ok = false;
        if ((flags & TYPE_TYPEPFX) == TYPE_TYPEPFX &&
                (c == '*' || c == '+' || c == '-')) {
            if (c == '*')
                return;
            else
                c = consume();
        }
        if (c == '[' && (flags & TYPE_RAWARRAY) == TYPE_RAWARRAY) {
            while ((c = consume()) == '[')
                ;
            if ((flags & TYPE_ARRAY) == TYPE_ARRAY)
                flags = TYPE_NORMAL;
            else
                flags = TYPE_RAWNORMAL;
        }
        switch (c) {
            case 'V':
                ok = (flags & TYPE_VOID) == TYPE_VOID;
                break;
            case 'B':case 'S':case 'C':case 'I':case 'J':case 'Z':case 'D':case 'F':
                ok = (flags & TYPE_BASE) == TYPE_BASE;
                break;
            case 'L':
                if ((flags & TYPE_RAWCLASS) == TYPE_RAWCLASS) {
                    // PackageSpecifier? SimpleClassTypeSignature[0]
                    boolean thenTypeArgOpt = (flags & TYPE_CLASS) == TYPE_CLASS;
                    collector.onIdent(identifier(IDENT_WITHPACKAGE | IDENT_THEN_TYPE_ARG_OPT));
                    if (thenTypeArgOpt) {
                        // SimpleClassTypeSignature[1]
                        if (peek() == '<')
                            parseTypeArguments();
                        // ClassTypeSignatureSuffix*
                        while (peek() == '.') { // ClassTypeSignatureSuffix
                            // TERMINAL "."
                            unpeek('.');
                            // SimpleClassTypeSignature
                            identifier(IDENT_WITHPACKAGE | IDENT_THEN_TYPE_ARG_OPT);
                            if (peek() == '<') { // TypeArguments?
                                // TypeArguments
                                parseTypeArguments();
                            }
                        }
                    }
                    expect(';');
                    ok = true;
                }
                break;
            case 'T':
                if ((flags & TYPE_TYPEVAR) == TYPE_TYPEVAR) {
                    identifier(0);
                    unpeek(';');
                    ok = true;
                }
                break;
        }
        if (!ok)
            throw new ParseException("Type Signature parse failed on " + flags);
    }

    public void parseFormalTypeParameters() throws ParseException {
        // FormalTypeParameters

        // <
        expect('<');
        // FormalTypeParameter+
        while (true) { // FormalTypeParameter
            // Identifier
            identifier(IDENT_THEN_COLON);
            // ClassBound
            unpeek(':');
            if (peek() != ':') { // FieldTypeSignatureopt
                parseTypeSignature(TYPE_FIELDTYPE);
            }
            // InterfaceBound*
            while (peek() == ':') {
                unpeek(':');
                parseTypeSignature(TYPE_FIELDTYPE); // FieldTypeSignature
            }
            if (peek() == '>')
                break;
        }
        unpeek('>');
    }

    protected void parseTypeArguments() throws ParseException {
        // TypeArguments

        // <
        expect('<');
        // TypeArgument+
        do {
            parseTypeSignature(TYPE_TYPEARG); // TypeArgument
        } while (peek() != '>');
        unpeek('>');
    }

    public void parseMethodTypeSignature() throws ParseException {
        // MethodTypeSignature

        // FormalTypeParameters?
        if (peek() == '<')
            parseFormalTypeParameters();

        // TERMINAL "("
        expect('(');

        // TypeSignature*
        while (peek() != ')') {
            parseTypeSignature(TYPE_NORMAL); // TypeSignature
        }

        // TERMINAL ")"
        unpeek(')');

        parseTypeSignature(TYPE_RETURNTYPE); // ReturnType

        while (peekOpt('\0') == '^') {
            unpeek('^');
            parseTypeSignature(TYPE_CLASS | TYPE_TYPEVAR); // ClassTypeSignature | TypeVariableSignature
        }

        expectEOF();
    }

    public void parseRawMethodTypeSignature() throws ParseException {
        expect('(');

        while (peek() != ')') {
            parseTypeSignature(TYPE_RAWNORMAL); // RawArgumentType
        }

        unpeek(')');

        parseTypeSignature(TYPE_RAWRETURNTYPE);
        expectEOF();
    }

    public void parseFieldTypeSignature() throws ParseException {
        parseTypeSignature(TYPE_FIELDTYPE);
        expectEOF();
    }

    public void parseRawFieldTypeSignature() throws ParseException {
        parseTypeSignature(TYPE_RAWNORMAL);
        expectEOF();
    }

    public void parseClassSignature() throws ParseException {
        // FormatTypeParameters?
        if (peek() == '<')
            parseFormalTypeParameters();

        // SuperclassSignature
        parseTypeSignature(TYPE_CLASS); // ClassTypeSignature

        // SuperinterfaceSignature*
        while (peekOpt('\0') == 'L')
            parseTypeSignature(TYPE_CLASS); // ClassTypeSignature

        expectEOF();
    }
}
