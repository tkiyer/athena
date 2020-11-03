/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tkiyer.athena.server.support;

import jdk.internal.org.objectweb.asm.ClassReader;
import org.eclipse.aether.artifact.Artifact;
import org.tkiyer.athena.engine.api.QueryEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walkFileTree;

// This is a hack for development and does not support nested classes.
final class QueryEngineDiscovery
{
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static final String SERVICES_FILE = "META-INF/services/" + QueryEngine.class.getName();

    private QueryEngineDiscovery() {}

    public static Set<String> discoverPlugins(Artifact artifact, ClassLoader classLoader)
            throws IOException
    {
        if (!artifact.getExtension().equals("athena-query-engine")) {
            throw new RuntimeException("Unexpected extension for main artifact: " + artifact);
        }

        File file = artifact.getFile();
        if (!file.getPath().endsWith("/target/classes")) {
            throw new RuntimeException("Unexpected file for main artifact: " + file);
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("Main artifact file is not a directory: " + file);
        }

        if (new File(file, SERVICES_FILE).exists()) {
            throw new RuntimeException("Unexpected services file in artifact directory: " + file);
        }

        return listClasses(file.toPath()).stream()
                .filter(name -> classInterfaces(name, classLoader).contains(QueryEngine.class.getName()))
                .collect(Collectors.toSet());
    }

    public static void writePluginServices(Iterable<String> plugins, File root)
            throws IOException
    {
        Path path = root.toPath().resolve(SERVICES_FILE);
        createDirectories(path.getParent());
        try (Writer out = new OutputStreamWriter(new FileOutputStream(path.toFile()), UTF_8)) {
            for (String plugin : plugins) {
                out.write(plugin + "\n");
            }
        }
    }

    private static List<String> listClasses(Path base)
            throws IOException
    {
        List<String> list = new ArrayList<>();
        walkFileTree(base, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
            {
                if (file.getFileName().toString().endsWith(CLASS_FILE_SUFFIX)) {
                    String name = file.subpath(base.getNameCount(), file.getNameCount()).toString();
                    list.add(javaName(name.substring(0, name.length() - CLASS_FILE_SUFFIX.length())));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return Collections.unmodifiableList(list);
    }

    private static List<String> classInterfaces(String name, ClassLoader classLoader)
    {
        List<String> list = new ArrayList<>();
        ClassReader reader = readClass(name, classLoader);
        for (String binaryName : reader.getInterfaces()) {
            list.add(javaName(binaryName));
        }
        if (reader.getSuperName() != null) {
            list.addAll(classInterfaces(javaName(reader.getSuperName()), classLoader));
        }
        return Collections.unmodifiableList(list);
    }

    private static ClassReader readClass(String name, ClassLoader classLoader)
    {
        try (InputStream in = classLoader.getResourceAsStream(binaryName(name) + CLASS_FILE_SUFFIX)) {
            if (in == null) {
                throw new RuntimeException("Failed to read class: " + name);
            }
            return new ClassReader(toByteArray(in));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String binaryName(String javaName)
    {
        return javaName.replace('.', '/');
    }

    private static String javaName(String binaryName)
    {
        return binaryName.replace('/', '.');
    }
}
