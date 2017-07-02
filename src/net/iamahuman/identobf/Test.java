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

import net.iamahuman.identobf.impl.asm.DescriptorParser;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luke1337 on 6/15/17.
 */
public class Test {

    Map<String, TestLoader> loaderMap = new HashMap<>();
    ClassLoader parentLoader = getClass().getClassLoader();
    URL baseUrl;
    private final Pattern entryPattern = Pattern.compile("^([^\\s]+)\\t([^ ]*) .*");

    protected class TestLoader extends URLClassLoader {

        String groupName;
        Map<String, String> ident2group;

        TestLoader(String groupName) throws IOException {
            super(new URL[] { new URL(baseUrl, groupName + "/") }, parentLoader);
            this.groupName = groupName;
            ident2group = loadResolutionMap(groupName);
        }

        @Override
        protected Class<?> loadClass(String s, boolean resolve) throws ClassNotFoundException {
            Class<?> clazz = findLoadedClass(s);
            if (clazz != null) return clazz;
            String groupName = ident2group.get(s);
            if (groupName != null && !this.groupName.equals(groupName)) {
                try {
                    return Test.this.getLoaderFor(groupName).loadClass(s, resolve);
                } catch (IOException e) {
                    throw new ClassNotFoundException(s, e);
                }
            }
            return super.loadClass(s, resolve);
        }

        @Override
        public Class<?> loadClass(String s) throws ClassNotFoundException {
            return loadClass(s, true);
        }

        public String toString() {
            return "TestLoader<" + groupName + ">";
        }
    }

    private Map<String, String> loadResolutionMap(String groupName) throws IOException {
        InputStream is = new URL(baseUrl, groupName + "/meta.txt").openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Map<String, String> map = new HashMap<>();
            for (String line; (line = reader.readLine()) != null; ) {
                Matcher m = entryPattern.matcher(line);
                if (m.matches()) map.put(m.group(1), m.group(2));
            }
            return map;
        } finally {
            is.close();
        }
    }

    private TestLoader getLoaderFor(String groupName) throws IOException {
        TestLoader loader = loaderMap.get(groupName);
        if (loader == null) loaderMap.put(groupName, loader = new TestLoader(groupName));
        return loader;
    }

    private Test(URL baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    public static void main(String[] args) throws Exception {
        String basePath = args[0], groupName = args[1], mainClass = args[2];

        Class<?> clazz = new Test(new File(basePath).toURI().toURL()).getLoaderFor(groupName).loadClass(mainClass);
        Method mainMethod = clazz.getDeclaredMethod("main", String[].class);

        int mod = mainMethod.getModifiers();
        if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
            String[] newArgs = new String[args.length - 3];
            System.arraycopy(args, 3, newArgs, 0, newArgs.length);
            mainMethod.invoke(null, (Object) newArgs);
        } else {
            throw new RuntimeException("not a static method");
        }
    }

}
