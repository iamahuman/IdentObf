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

import jdk.internal.org.objectweb.asm.Opcodes;
import net.iamahuman.identobf.annots.*;
import net.iamahuman.identobf.graphs.*;
import net.iamahuman.identobf.impl.asm.*;
import net.iamahuman.identobf.nodes.*;
import net.iamahuman.identobf.nodes.Group;
import net.iamahuman.identobf.user.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luke1337 on 6/6/17.
 */
@Colour("net.iamahuman.identobf.Main")
@net.iamahuman.identobf.annots.Group("main")
public class Main {
    private static Pattern anonPattern = Pattern.compile(".*\\$([1-9][0-9]*)$");

    private static void printUsageAndExit() {
        System.err.println("Usage: java net.iamahuman.identobf.Main input_dir output_dir packages..");
        System.exit(1);
    }

    private static void printCounter(long start, String msg) {
        final long end = System.currentTimeMillis();
        System.err.println("Done! (" + msg + ", " + ((end - start) / 1000.) + "s)");
    }

    private static String group2ident(Group group) {
        Object label = group.label;
        if (label instanceof String)
            return (String) label;
        return "g_" + ((ItemGroups.AnonymousName) group.label).getId();
    }

    private static boolean deleteRecursive(File... files) {
        for (File f : files) {
            if ((f.isDirectory() && !deleteRecursive(f.listFiles())) || !f.delete())
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            printUsageAndExit();
            return;
        }
        File inputDir = new File(args[0]), outputDir = new File(args[1]);

        if (!inputDir.isDirectory()) {
            System.err.println("input_dir is not a directory");
            printUsageAndExit();
            return;
        }

        if (outputDir.isDirectory()) {
            deleteRecursive(outputDir.listFiles());
        } else if (!outputDir.mkdirs()) {
            System.err.println("output_dir is not a directory");
            printUsageAndExit();
            return;
        }

        URL inputDirURL;
        try {
            inputDirURL = inputDir.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            printUsageAndExit();
            return;
        }

        String[] pkgNames = new String[args.length - 2];
        for (int i = 0; i < args.length - 2; i++)
            pkgNames[i] = args[i + 2].replace(".", "/");

        long counter;
        long start = System.currentTimeMillis();
        try {
            System.err.println("Collecting..");
            counter = System.currentTimeMillis();
            RequestRegistry registry = new RequestRegistry(new ClasspathItemResolver<>(
                    new AdhocClasspath(new Config(), inputDirURL), IdentityMapper.identity()));
            registry.requestPackages(pkgNames);
            registry.addRequestMetaAdapters(new AsmClassAccessRequestAdapter(), new MainSpecifierAdapter());
            Collection<RequestMeta> metas = registry.getRequests();
            printCounter(counter, "collected " + metas.size() + " requests");

            System.err.println("Creating graph..");
            counter = System.currentTimeMillis();
            Graph graph = new Graph(metas);
            ItemGroups groups = graph.getGroups();
            printCounter(counter, "created graph with " + graph.getSubgraphs().size() +
                    " subgraphs and " + groups.getGroups().size() + " groups");

            System.err.println("Validating (pass #1)..");
            for (ConnectedGraph subgraph : graph.getSubgraphs()) {
                subgraph.validate();
                System.err.println("Subgraph " + subgraph + ": ");
                for (Node n : subgraph.getNodes()) {
                    for (Item i : n.getItems()) {
                        System.err.println("\t" + i.getOriginalIdentifier());
                    }
                }
            }
            printCounter(counter, "#1");

            System.err.println("Validating (pass #2)..");
            for (ConnectedGraph subgraph : graph.getSubgraphs()) {
                System.err.println("Subgraph " + subgraph + ": ");
                for (Clique c : subgraph.getCliques())
                    c.validate();
                int nontrivial = 0, trivial = 0, edges = 0;
                for (Node n : subgraph.getNodes()) {
                    n.validate();
                    edges += n.getEdges().size();
                    if (n.getEdges().isEmpty())
                        trivial++;
                    else
                        nontrivial++;
                }
                System.err.println("\t" + trivial + " trivial, " + nontrivial + " nontrivial, " + edges + " edges");
            }
            printCounter(counter, "#2");

            System.err.println("Colouring..");
            counter = System.currentTimeMillis();
            GraphColourer colourer = new SimpleGraphColourer(graph);
            GraphColourCollector collector = new GraphColourCollector();
            colourer.colour(collector);
            printCounter(counter, "graph has " + collector.getChroma() + " colours");

            System.err.println("Validating (pass #3)..");
            counter = System.currentTimeMillis();
            for (Clique c : graph.getCliques()) {
                c.validate();
                c.validateColours();
            }
            printCounter(counter, "#3");

            System.err.println("Aggregating..");
            counter = System.currentTimeMillis();
            Map<Item, String> map = collector.getColourMap(new ColourNamer<String>() {
                private AsmItem itemFor(Node node) {
                    Iterator<? extends Item> items = node.getItems().iterator();
                    if (!items.hasNext()) return null;
                    AsmItem sole = (AsmItem) items.next();
                    if (items.hasNext()) return null;
                    return sole;
                }

                private String partFor(Node node, Map<Integer, Object> forcedColours) {
                    Object forced = forcedColours.get(node.getColourIndex());
                    if (forced != null) return forced.toString();
                    AsmItem item = itemFor(node);
                    if (item != null) {
                        String orig = item.getOriginalIdentifier().toString();
                        Matcher m = anonPattern.matcher(orig);
                        if (m.matches())
                            return m.group(1);
                    }
                    return "anon_" + node.getColourIndex();
                }

                @Override
                public String identifierFor(Node node, Map<Integer, Object> forcedColours) {
                    String id = partFor(node, forcedColours);
                    try {
                        while (true) {
                            AsmItem sole = itemFor(node);
                            if (sole == null) break;
                            AsmOuterClassReference outerRef = new AsmOuterClassReference(sole, sole.getResult(), false);
                            if (outerRef.getIdentifier() == null) break;
                            AsmItem theItem = (AsmItem) outerRef.getSource().getResolver().resolve(outerRef);
                            node = graph.getNode(theItem);
                            if (node == null) break;
                            id = partFor(node, forcedColours) + "$" + id;
                        }
                    } catch (ClassResolutionException ignored) {
                    }
                    return id;
                }
            });
            printCounter(counter, "retrieved " + map.size() + " entries");

            System.err.println("Applying..");
            counter = System.currentTimeMillis();
            ItemRenamer ren = new ItemRenamer(graph.getItems(), groups, map);
            printCounter(counter, "coloured");

            Map<Group, File> group2dir = new HashMap<>();

            for (Map.Entry<Group, Collection<Item>> entry :
                    new GroupRefMapper(graph.getCliques()).getGroupRefMap().entrySet()) {
                final Group group = entry.getKey();
                final String groupName = group2ident(group);
                final Collection<Item> values = entry.getValue();

                File f = new File(outputDir, groupName);
                group2dir.put(group, f);
                if (!f.mkdirs() && !f.isDirectory())
                    throw new IOException("directory creation failure: " + f);

                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f, "meta.txt")));
                try {
                    for (Item referee : values) {
                        bw.append(referee.getName())
                                .append("\t")
                                .append(group2ident(groups.getGroup(referee)))
                                .append(" (")
                                .append(referee.getOriginalIdentifier().toString())
                                .append(")\n");
                    }
                } finally {
                    bw.close();
                }
            }

            System.err.println("Writing..");
            counter = System.currentTimeMillis();
            SortedMap<String, File> fileMap = new TreeMap<>();
            for (Map.Entry<Item, String> entry : map.entrySet()) {
                final Item key = entry.getKey();
                final String value = entry.getValue();

                if (key instanceof AsmItem) {
                    final ClassNode node = ((AsmItem) key).getResult();
                    final Group group = groups.getGroup(key);
                    final String orig = key.getOriginalIdentifier().toString();

                    File fi = new File(group2dir.get(group),
                            node.name.replace("/", File.separator) + ".class");
                    File par = fi.getParentFile();
                    if (!par.mkdirs() && !par.isDirectory())
                        throw new IOException("mkdirs " + par + " failure");
                    fileMap.put(orig, fi);

                    ClassWriter cw = new ClassWriter(0);
                    node.accept(cw);

                    OutputStream is = new FileOutputStream(fi);
                    try {
                        is.write(cw.toByteArray());
                    } finally {
                        is.close();
                    }
                }
            }
            System.out.flush();
            System.err.flush();
            for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
            System.out.flush();
            printCounter(counter, "I/O");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            printCounter(start, "total");
            System.err.flush();
        }
    }

    private static class MainSpecifierAdapter implements RequestMetaAdapter {
        @Override
        public RequestMeta getMeta(RequestRegistry registry, Item item) {
            ClassNode node = ((AsmItem) item).getResult();
            node.sourceFile = null;
            String groupId = null, fixedColour = null;
            if (node.name.endsWith("/Main")) for (MethodNode mn : node.methods) {
                if ("main".equals(mn.name) && "([Ljava/lang/String;)V".equals(mn.desc)) {
                    groupId = "g_main";
                    fixedColour = "Main";
                    break;
                }
            }
            return new RequestMeta(item, groupId, fixedColour, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList());
        }
    }
}
