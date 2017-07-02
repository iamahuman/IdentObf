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

import net.iamahuman.identobf.user.ClassResolutionException;
import net.iamahuman.identobf.user.Classpath;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by luke1337 on 6/14/17.
 */
public class AdhocClasspath implements Classpath<AsmItem, String> {

    private final Config config;
    private final URL baseUrl;
    private static final boolean DEBUG = false;
    private static final String TAG = AdhocClasspath.class.getSimpleName();

    public AdhocClasspath(Config config, URL baseUrl) {
        super();
        this.config = config;
        this.baseUrl = baseUrl;
    }

    protected ClassNode yieldClass(ClassReader reader) {
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        return cn;
    }

    @Override
    public AsmItem loadItem(String identifier) throws ClassResolutionException {
        if (identifier.contains(";") || identifier.contains(".") || identifier.contains("\\"))
            throw new ClassResolutionException("Invalid identifier: " + identifier);
        InputStream stream;
        try {
            stream = new URL(baseUrl, identifier + ".class").openStream();
        } catch (MalformedURLException e) {
            throw new ClassResolutionException("Invalid identifier: " + identifier, e);
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
            return null;
        }
        ClassReader reader;
        ClassNode node;
        try {
            try {
                reader = new ClassReader(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new ClassResolutionException("Failed to read class " + identifier, e);
        }
        node = yieldClass(reader);
        if (!node.name.equals(identifier))
            throw new ClassResolutionException("Invalid class name: specified " + identifier + "; got " + node.name);
        return new AsmItem(config, node, identifier);
    }

    @Override
    public Collection<String> findClassesInPackage(String pkgName) throws ClassResolutionException {
        if (DEBUG)
            System.err.println(TAG + ": findClassesInPackage start");
        if (baseUrl.getProtocol().equalsIgnoreCase("file")) {
            final String path = baseUrl.getPath();
            if (!path.endsWith("/") && !path.endsWith("%2F"))
                throw new ClassResolutionException("file:// URL is not a directory");
            File baseDir;
            try {
                baseDir = new File(baseUrl.toURI());
            } catch (URISyntaxException e) {
                baseDir = new File(baseUrl.getPath());
            }
            String basename;
            File target;
            if (pkgName == null) {
                basename = null;
                target = baseDir;
            } else {
                String[] split = pkgName.split("/");
                StringBuilder basenameBuilder = new StringBuilder();
                StringBuilder pathBuilder = new StringBuilder();
                int i;
                for (i = 0; i < split.length - 1; i++) {
                    basenameBuilder.append(split[i]).append('/');
                    pathBuilder.append(split[i]).append(File.separatorChar);
                }
                pathBuilder.append(split[i]);
                basename = basenameBuilder.toString();
                target = new File(baseDir, pathBuilder.toString());
            }
            if (target.isDirectory()) {
                Collection<String> names = new ArrayList<>();
                collectClasses(names, basename, target, config.maxDepth);
                return names;
            }
            throw new ClassResolutionException("destination " + target + " is not a directory");
        }
        throw new ClassResolutionException("unsupported URL for listing: " + baseUrl);
    }

    private void collectClasses(Collection<String> collection, String pkgName, File item, int depth) {
        if (DEBUG)
            System.err.println(TAG + ": Visiting " + item);
        if (depth > 0) {
            File[] files = item.listFiles();
            if (files != null) {
                final int newDepth = depth - 1;
                final String subpkg = pkgName + item.getName() + "/";
                for (File f : files)
                    collectClasses(collection, subpkg, f, newDepth);
            }
        }
        if (item.isFile()) {
            final String name = item.getName();
            final String suffix = ".class";
            if (name.endsWith(suffix)) {
                final String clsName = name.substring(0, name.length() - suffix.length());
                if (!clsName.contains(";") && !clsName.contains(".") && !clsName.contains("\\")) {
                    final String fqn = pkgName + clsName;
                    if (DEBUG)
                        System.err.println(TAG + ": Adding " + fqn);
                    collection.add(fqn);
                }
            }
        }
    }

    public URL getBaseUrl() {
        return baseUrl;
    }
}
