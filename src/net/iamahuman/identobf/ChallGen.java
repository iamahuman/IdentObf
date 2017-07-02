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

import net.iamahuman.identobf.graphs.*;
import net.iamahuman.identobf.impl.asm.*;
import net.iamahuman.identobf.nodes.*;
import net.iamahuman.identobf.user.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

/**
 * Created by luke1337 on 6/25/17.
 */
public class ChallGen {
    private static boolean deleteDirectoryContents(File dir) {
        File[] fs = dir.listFiles();
        if (fs != null) for (File f : fs) {
            if (((f.isDirectory() && !deleteDirectoryContents(f)) || !f.delete()) && !f.exists())
                return false;
        }
        return true;
    }

    private static int writeClass(AsmItem ai, PrintWriter bw, int v, File coutDir) throws IOException {
        //bw.print("static uint32_t ");
        //bw.print(name);
        //bw.print("[] = {");
        ClassWriter cw = new ClassWriter(0);
        ai.getResult().accept(cw);
        byte[] data = cw.toByteArray();
        int bk = 0xf7a13cdd;
        for (int rr = 0; rr < v; rr++)
            bk *= 17;
        Random r = new SecureRandom();
        for (int i = 0; i < data.length; i += 4) {
            if (i != 0) {
                bw.print(",");
                if (i % 64 == 0)
                    bw.println();
            }
            // Assume little endian
            int dw = 0;
            for (int j = 0; j < 4; j++) {
                int k = (i + j < data.length) ? (data[i + j] & 0xff) : r.nextInt(256);
                dw |= k << (8 * j);
            }
            int ui = i / 4;
            int rotamount = ((ui + 17) * (ui + 34) - v) & 31;
            if (rotamount > 0)
                dw = (dw << rotamount) | (dw >>> (32 - rotamount));
            int xoramount = ((ui + 23) * (ui + 59)) + bk;
            dw ^= xoramount;
            int addamount = (ui + 0xdca12149 * v) * 0xcad71f49;
            dw += addamount;
            bw.print("0x");
            bw.print(Integer.toHexString(dw));
        }
        //bw.println("};");
        String outName = ai.getOriginalIdentifier().toString().replace("/", ".");
        try (OutputStream os = new FileOutputStream(new File(coutDir,
                "g" + v + "_" + outName + ".class"))) {
            os.write(data);
        }
        return data.length;
    }
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
        URL inputDir = kbaseDir.toURI().toURL();
        Config config = new Config();
        lolol(kbaseDir, outDir);
        Remapper rm = new SimpleRemapper("com/secuinside/ctf/cliquel/obfzone/stepone/Access.a2()Ljava/lang/String;", "a");
        RequestRegistry registry = new RequestRegistry(new ClasspathItemResolver<Item, String>(new AdhocClasspath(config, inputDir) {
            @Override
            protected ClassNode yieldClass(ClassReader reader) {
                ClassNode cn = new ClassNode();
                reader.accept(new ClassRemapper(cn, rm), 0);
                return cn;
            }
        }, IdentityMapper.identity()));
        registry.addRequestMetaAdapters(new AsmAnnotationAdapter(), new AsmClassAccessRequestAdapter(), new RequestMetaAdapter() {
            @Override
            public RequestMeta getMeta(RequestRegistry registry, Item item) throws ConstraintException {
                ClassNode cnode = ((AsmItem) item).getResult();
                for (MethodNode mn : cnode.methods) {
                    /*
                    if (mn.parameters != null) for (ParameterNode pn : mn.parameters)
                        pn.name = null;
                    if (mn.localVariables != null) for (LocalVariableNode lvar : mn.localVariables)
                        lvar.name = null;
                    */
                    mn.parameters = null;
                    mn.localVariables = null;
                    Iterator<? extends AbstractInsnNode> it = mn.instructions.iterator();
                    while (it.hasNext()) {
                        AbstractInsnNode node = it.next();
                        if (node instanceof LineNumberNode)
                            it.remove();
                    }
                    mn.access |= Opcodes.ACC_SYNTHETIC;
                }
                for (FieldNode fn : cnode.fields)
                    fn.access |= Opcodes.ACC_SYNTHETIC;
                cnode.sourceFile = null;
                cnode.sourceDebug = null;
                return null;
            }
        });
        registry.requestPackage("com/secuinside/ctf/cliquel/obfzone");
        Graph graph = new Graph(registry.getRequests());
        GraphColourer gc = new SimpleGraphColourer(graph);
        GraphColourCollector collector = new GraphColourCollector();
        gc.colour(collector);
        Map<Item, String> map = collector.getColourMap(new ColourNamer<String>() {
            Map<Integer, String> myMap = new HashMap<>();
            @Override
            public String identifierFor(Node node, Map<Integer, Object> forcedColours) {
                int ci = node.getColourIndex();
                Object fc = forcedColours.get(ci);
                if (fc != null) return fc.toString();
                String fcn = myMap.get(ci);
                if (fcn != null) return fcn;
                StringBuilder nameb = new StringBuilder("com/secuinside/ctf/cliquel/Main");
                for (int i = 0; i < 2; i++) {
                    int insi = (ci * 347 + 1049 + i * i) % (nameb.length() + 1);
                    nameb.insert(insi, '\u200C');
                }
                String name = nameb.toString();
                myMap.put(ci, name);
                return name;
            }
        });
        ItemGroups ig = graph.getGroups();
        //map = Collections.emptyMap();
        ItemRenamer ir = new ItemRenamer(registry.getItems(), ig, map);
        int groupId = 0;
        Map<Group, Integer> group2int = new HashMap<>();
        Map<Integer, Group> int2group = new HashMap<>();
        List<Group> grouplist = new ArrayList<>();
        Group hookDestGroup = null, hookSrcGroup = null;
        for (Group group : ig.getGroups()) {
            Object origIdent = group.items.iterator().next().getOriginalIdentifier();
            if ("com/secuinside/ctf/cliquel/obfzone/stepone/StepThree".equals(origIdent))
                hookDestGroup = group;
            else if ("com/secuinside/ctf/cliquel/obfzone/stepone/StepThreeHook".equals(origIdent)) {
                hookSrcGroup = group;
                continue;
            }
            System.out.println(groupId + " => " + group.items.iterator().next().getName().replace('\u200C', '!'));
            int2group.put(groupId, group);
            group2int.put(group, groupId++);
            grouplist.add(group);
        }
        SortedMap<Integer, SortedSet<Integer>> flagGraph = new TreeMap<>();
        for (Map.Entry<Group, Collection<Item>> entry : ir.getReferences().entrySet()) {
            Group k = entry.getKey();
            if (k == hookSrcGroup) continue;
            Collection<Item> v = entry.getValue();
            int ki = group2int.get(k);
            SortedSet<Integer> ss = new TreeSet<>();
            Set<String> assertionSet = new HashSet<>();
            assert k.items.size() == 1;
            assert assertionSet.add(k.items.iterator().next().getName());
            for (Item i : v) {
                assert assertionSet.add(i.getName());
                Group rfeg = ig.getGroup(i);
                if (rfeg == null) {
                    //throw new RuntimeException("unknown group for " + i.getOriginalIdentifier());
                    continue; // probably an external reference
                }
                int rfegidx = group2int.get(rfeg);
                ss.add(rfegidx);
            }
            flagGraph.put(ki, ss);
        }
        StringBuilder sb = new StringBuilder();
        int lg = -1;
        PrintWriter pwk = new PrintWriter("/tmp/out.txt");
        for (Map.Entry<Integer, SortedSet<Integer>> entry : flagGraph.entrySet()) {
            int k = entry.getKey();
            int li = -1;
            for (int i : entry.getValue()) {
                if (i != k) { // group may always reference itself
                    if (lg != k) {
                        pwk.println("group " + k + " <" + int2group.get(k).items.iterator().next().getOriginalIdentifier() + "> {");
                        sb.append((char) ('a' + (k - lg) - 1));
                        lg = k;
                    }
                    pwk.println("\t" + i + " <" + int2group.get(i).items.iterator().next().getOriginalIdentifier() + ">");
                    sb.append((char) ('a' + (i - li) - 1));
                    li = i;
                }
            }
            if (lg == k) {
                pwk.println("}");
                sb.append((char) ('a' + (groupId - li) - 1));
            }
        }
        pwk.close();
        sb.append((char) ('a' + (groupId - lg) - 1));
        try (PrintWriter flagw = new PrintWriter(new File(outDir, "flag.txt"))) {
            flagw.println(sb.toString());
        }
        try (PrintWriter gcw = new PrintWriter(new File(outDir, "groupcnt.h"))) {
            Group mainGroup = null;
            for (RequestMeta meta : ig.getRequests()) {
                if ("com/secuinside/ctf/cliquel/obfzone/Main".equals(meta.item.getOriginalIdentifier())) {
                    mainGroup = ig.getGroup(meta.item);
                    break;
                }
            }
            gcw.printf("#define GROUP_COUNT %d\n#define MAIN_INDEX %d\n",
                    groupId, group2int.get(mainGroup));
        }
        try (PrintWriter bw = new PrintWriter(new File(outDir, "groups.h"))) {
            Map<Group, Integer> group2size = new HashMap<>();
            Map<Group, Integer> group2off = new HashMap<>();
            File coutDir = new File(outDir, "out");
            deleteDirectoryContents(coutDir);
            coutDir.mkdirs();
            bw.println("static uint32_t cdat[] = {");
            int off = 0;
            List<Map.Entry<Group, Integer>> entriesList = new ArrayList<>(group2int.entrySet());
            Collections.shuffle(entriesList, new SecureRandom());
            boolean first = true;
            for (Map.Entry<Group, Integer> entry : entriesList) {
                Group k = entry.getKey();
                int v = entry.getValue();
                ClassWriter cw = new ClassWriter(0);
                if (k.items.size() != 1)
                    throw new RuntimeException("wtf");
                AsmItem ai = (AsmItem) k.items.iterator().next();
                if (first) first = false;
                else bw.println(",");
                int size = writeClass(ai, bw, v, coutDir);
                group2off.put(k, off);
                group2size.put(k, size);
                off += (size + 3) / 4;
            }
            bw.println("};");
            bw.println("#ifdef HOOKMODE");
            bw.println("static uint32_t cdathook[] = {");
            int hookSize = writeClass((AsmItem) hookSrcGroup.items.iterator().next(), bw, group2int.get(hookDestGroup), coutDir);
            bw.println("};");
            bw.println("#endif");
            bw.println("static struct group groups[] = {");
            int i = 0;
            for (Group g : grouplist) {
                int sz = group2size.get(g);
                if (i != 0) {
                    bw.print(",");
                    if (i % 64 == 0)
                        bw.println();
                }
                boolean isTG = g.equals(hookDestGroup);
                if (isTG) {
                    bw.println();
                    bw.println("#ifdef HOOKMODE");
                    bw.printf("{%d,%s}\n", hookSize, "cdathook");
                    bw.println("#else");
                }
                bw.printf("{%d,cdat%+d}", sz, group2off.get(g));
                if (isTG) {
                    bw.println();
                    bw.println("#endif");
                }
                i++;
            }
            bw.println("};");
        }
    }
}
