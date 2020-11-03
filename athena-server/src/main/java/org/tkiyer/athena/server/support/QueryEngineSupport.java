package org.tkiyer.athena.server.support;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkiyer.athena.engine.api.QueryEngine;
import org.tkiyer.athena.server.config.QueryEngineConfiguration;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.tkiyer.athena.server.support.QueryEngineDiscovery.discoverPlugins;
import static org.tkiyer.athena.server.support.QueryEngineDiscovery.writePluginServices;

@ThreadSafe
public class QueryEngineSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(QueryEngineSupport.class);

    private final static List<String> API_PACKAGES = Collections.unmodifiableList(Arrays.asList("org.tkiyer.athena.engine.api", "com.fasterxml.jackson.annotation.", "org.openjdk.jol."));

    private final ArtifactResolver resolver;

    private final QueryEngineManager queryEngineManager;

    private final File installedQueryEnginesDir;

    private final List<String> queryEngines;

    private final AtomicBoolean queryEnginesLoading = new AtomicBoolean();

    private final AtomicBoolean queryEnginesLoaded = new AtomicBoolean();

    @Inject
    public QueryEngineSupport(QueryEngineConfiguration conf) {
        requireNonNull(conf, "config is null");
        this.installedQueryEnginesDir = conf.getQueryEngineDirectory();
        if (conf.getQueryEngines() == null) {
            this.queryEngines = Collections.unmodifiableList(Collections.emptyList());
        } else {
            this.queryEngines = Collections.unmodifiableList(conf.getQueryEngines());
        }

        this.resolver = new ArtifactResolver(conf.getMavenLocalRepository(), conf.getMavenRemoteRepository());

        this.queryEngineManager = new QueryEngineManager();
    }

    public void loadQueryEngines() throws Exception {
        if (!queryEnginesLoading.compareAndSet(false, true)) {
            return;
        }

        for (File file : listFiles(installedQueryEnginesDir)) {
            if (file.isDirectory()) {
                loadQueryEngine(file.getAbsolutePath());
            }
        }

        for (String queryEngine : queryEngines) {
            loadQueryEngine(queryEngine);
        }

        queryEnginesLoaded.set(true);
    }

    private void loadQueryEngine(String queryEngine) throws Exception {
        LOGGER.info("-- Loading query engine %s --", queryEngine);
        QueryEngineClassLoader queryEngineClassLoader = buildClassLoader(queryEngine);
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(queryEngineClassLoader)) {
            loadQueryEngine(queryEngineClassLoader);
        }
        LOGGER.info("-- Finished loading query engine %s --", queryEngine);
    }

    private void loadQueryEngine(QueryEngineClassLoader queryEngineClassLoader) {
        ServiceLoader<QueryEngine> serviceLoader = ServiceLoader.load(QueryEngine.class, queryEngineClassLoader);
        List<QueryEngine> queryEngines = new ArrayList<>();
        for (QueryEngine queryEngine : serviceLoader) {
            queryEngines.add(queryEngine);
        }

        checkState(!queryEngines.isEmpty(), "No service providers of type %s", QueryEngine.class.getName());
        for (QueryEngine queryEngine : queryEngines) {
            LOGGER.info("Installing %s", queryEngine.getClass().getName());
            installQueryEngine(queryEngine, queryEngineClassLoader::duplicate);
        }
    }

    public void installQueryEngine(QueryEngine queryEngine, Supplier<ClassLoader> duplicateQueryEngineClassLoaderFactory) {
        LOGGER.info("Registering query engine %s", queryEngine.getName());
        queryEngineManager.addQueryEngine(queryEngine, duplicateQueryEngineClassLoaderFactory);
    }

    private QueryEngineClassLoader buildClassLoader(String queryEngine)
            throws Exception {
        File file = new File(queryEngine);
        if (file.isFile() && (file.getName().equals("pom.xml") || file.getName().endsWith(".pom"))) {
            return buildClassLoaderFromPom(file);
        }
        if (file.isDirectory()) {
            return buildClassLoaderFromDirectory(file);
        }
        return buildClassLoaderFromCoordinates(queryEngine);
    }

    private QueryEngineClassLoader buildClassLoaderFromPom(File pomFile)
            throws Exception {
        List<Artifact> artifacts = resolver.resolvePom(pomFile);
        QueryEngineClassLoader classLoader = createClassLoader(artifacts, pomFile.getPath());

        Artifact artifact = artifacts.get(0);
        Set<String> plugins = discoverPlugins(artifact, classLoader);
        if (!plugins.isEmpty()) {
            File root = new File(artifact.getFile().getParentFile().getCanonicalFile(), "query-engine-discovery");
            writePluginServices(plugins, root);
            LOGGER.debug("    %s", root);
            classLoader = classLoader.withUrl(root.toURI().toURL());
        }

        return classLoader;
    }

    private QueryEngineClassLoader buildClassLoaderFromDirectory(File dir)
            throws Exception {
        LOGGER.debug("Classpath for %s:", dir.getName());
        List<URL> urls = new ArrayList<>();
        for (File file : listFiles(dir)) {
            LOGGER.debug("    %s", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private QueryEngineClassLoader buildClassLoaderFromCoordinates(String coordinates)
            throws Exception {
        Artifact rootArtifact = new DefaultArtifact(coordinates);
        List<Artifact> artifacts = resolver.resolveArtifacts(rootArtifact);
        return createClassLoader(artifacts, rootArtifact.toString());
    }

    private QueryEngineClassLoader createClassLoader(List<Artifact> artifacts, String name)
            throws IOException {
        LOGGER.debug("Classpath for %s:", name);
        List<URL> urls = new ArrayList<>();
        for (Artifact artifact : sortedArtifacts(artifacts)) {
            if (artifact.getFile() == null) {
                throw new RuntimeException("Could not resolve artifact: " + artifact);
            }
            File file = artifact.getFile().getCanonicalFile();
            LOGGER.debug("    %s", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private QueryEngineClassLoader createClassLoader(List<URL> urls) {
        ClassLoader parent = getClass().getClassLoader();
        return new QueryEngineClassLoader(urls, parent, API_PACKAGES);
    }

    private static List<File> listFiles(File installedPluginsDir) {
        if (installedPluginsDir != null && installedPluginsDir.isDirectory()) {
            File[] files = installedPluginsDir.listFiles();
            if (files != null) {
                Arrays.sort(files);
                return ImmutableList.copyOf(files);
            }
        }
        return ImmutableList.of();
    }

    private static List<Artifact> sortedArtifacts(List<Artifact> artifacts) {
        List<Artifact> list = new ArrayList<>(artifacts);
        list.sort(Ordering.natural().nullsLast().onResultOf(Artifact::getFile));
        return list;
    }
}
