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

package net.iamahuman.identobf;

import org.objectweb.asm.*;
import java.io.*;

/**
 * Created by luke1337 on 6/29/17.
 */
public class MainGen {
    private static void lolol(File kbaseDir, File outDir) throws IOException {
        final String path = "com/secuinside/ctf/cliquel/Main.class";
        File lchr = new File(kbaseDir, path);
        File lkchr = new File(outDir, path);
        lkchr.getParentFile().mkdirs();
        ClassReader cr;
        try (FileInputStream fis = new FileInputStream(lchr)) {
            cr = new ClassReader(fis);
        }
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visitSource(String source, String debug) {
                super.visitSource(null, null);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                boolean hide = true;
                if ("main".equals(name) && "([Ljava/lang/String;)V".equals(desc))
                    hide = false;
                if ("<init>".equals(name) && "(Ljava/lang/ClassLoader;)V".equals(desc))
                    hide = false;
                if (hide)
                    access |= Opcodes.ACC_SYNTHETIC;
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(api, mv) {
                    @Override
                    public void visitParameter(String name, int access) {
                        //super.visitParameter(name, access);
                    }

                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        //super.visitLocalVariable(name, desc, signature, start, end, index);
                    }

                    @Override
                    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
                        //return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
                        return null;
                    }

                    @Override
                    public void visitLineNumber(int line, Label start) {
                        //super.visitLineNumber(line, start);
                    }
                };
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                return super.visitField(access | Opcodes.ACC_SYNTHETIC, name, desc, signature, value);
            }
        };
        cr.accept(cv, 0);
        try (FileOutputStream fos = new FileOutputStream(lkchr)) {
            fos.write(cw.toByteArray());
        }
    }
    public static void main(String[] args) throws Exception {
        File theBaseDir = new File("/tmp/workdir");
        File outDir = new File(theBaseDir, "native/");
        File kbaseDir = new File(theBaseDir, "IdentObf/out/production/Chall/");
        lolol(kbaseDir, outDir);
    }
}
