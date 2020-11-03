package org.tkiyer.athena.server.support;

import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class ArtifactResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResolver.class);

    public static final String USER_LOCAL_REPO = System.getProperty("user.home") + "/.m2/repository";

    public static final String MAVEN_CENTRAL_URI = "http://repo1.maven.org/maven2/";

    private static final int LOGGER_LEVEL_INFO = 1;

    private final RepositorySystem repositorySystem;

    private final RepositorySystemSession repositorySystemSession;

    private final List<RemoteRepository> repositories;

    public ArtifactResolver(String localRepositoryDir, String... remoteRepositoryUris) {
        this(localRepositoryDir, Arrays.asList(remoteRepositoryUris));
    }

    public ArtifactResolver(String localRepositoryDir, List<String> remoteRepositoryUris) {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        locator.addService(RepositoryConnectorFactory.class, AsyncRepositoryConnectorFactory.class);
        repositorySystem = locator.getService(RepositorySystem.class);
        DefaultRepositorySystemSession defaultRepositorySystemSession = new DefaultRepositorySystemSession();
        LocalRepositoryManager localRepositoryManager = null;
        try {
            localRepositoryManager = new SimpleLocalRepositoryManagerFactory().newInstance(defaultRepositorySystemSession, new LocalRepository(localRepositoryDir));
        } catch (NoLocalRepositoryManagerException e) {
            LOGGER.warn("No local repo.", e);
        }
        defaultRepositorySystemSession.setTransferListener(new ConsoleTransferListener());
        defaultRepositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener());
        defaultRepositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        this.repositorySystemSession = defaultRepositorySystemSession;

        List<RemoteRepository> repositories = new ArrayList<>(remoteRepositoryUris.size());
        int index = 0;
        for (String repositoryUri : remoteRepositoryUris) {
            repositories.add(new RemoteRepository.Builder("repo-" + index++, "default", repositoryUri).build());
        }
        this.repositories = Collections.unmodifiableList(repositories);
    }

    public List<Artifact> resolveArtifacts(Artifact... sourceArtifacts)
    {
        return resolveArtifacts(Arrays.asList(sourceArtifacts));
    }

    public List<Artifact> resolveArtifacts(Iterable<? extends Artifact> sourceArtifacts)
    {
        CollectRequest collectRequest = new CollectRequest();
        for (Artifact sourceArtifact : sourceArtifacts) {
            collectRequest.addDependency(new Dependency(sourceArtifact, JavaScopes.RUNTIME));
        }
        for (RemoteRepository repository : repositories) {
            collectRequest.addRepository(repository);
        }

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME));

        return resolveArtifacts(dependencyRequest);
    }

    public List<Artifact> resolvePom(File pomFile)
    {
        if (pomFile == null) {
            throw new RuntimeException("pomFile is null");
        }

        MavenProject pom = getMavenProject(pomFile);
        Artifact rootArtifact = getProjectArtifact(pom);

        CollectRequest collectRequest = new CollectRequest();
        for (org.apache.maven.model.Dependency dependency : pom.getDependencies()) {
            collectRequest.addDependency(toAetherDependency(dependency));
        }
        for (RemoteRepository repository : pom.getRemoteProjectRepositories()) {
            collectRequest.addRepository(repository);
        }
        for (RemoteRepository repository : repositories) {
            collectRequest.addRepository(repository);
        }

        // Make sure we account for managed dependencies
        if (pom.getDependencyManagement() != null) {
            for (org.apache.maven.model.Dependency managedDependency : pom.getDependencyManagement().getDependencies()) {
                collectRequest.addManagedDependency(toAetherDependency(managedDependency));
            }
        }

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME));
        List<Artifact> artifacts = resolveArtifacts(dependencyRequest);

        Map<String, Artifact> modules = getSiblingModules(pom).stream()
                .collect(toMap(ArtifactResolver::getArtifactKey, identity()));

        return Stream.concat(
                Stream.of(rootArtifact),
                artifacts.stream()
                        .map(artifact -> modules.getOrDefault(getArtifactKey(artifact), artifact)))
                .collect(Collectors.toList());
    }

    private MavenProject getMavenProject(File pomFile)
    {
        try {
            PlexusContainer container = container();
            ProjectBuilder projectBuilder = container.lookup(ProjectBuilder.class);
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
            request.setSystemProperties(requiredSystemProperties());
            request.setRepositorySession(repositorySystemSession);
            request.setProcessPlugins(false);
            ProjectBuildingResult result = projectBuilder.build(pomFile, request);
            return result.getProject();
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading pom: " + pomFile.getAbsolutePath(), e);
        }
    }

    private Artifact getProjectArtifact(MavenProject pom)
    {
        return new DefaultArtifact(
                pom.getArtifact().getGroupId(),
                pom.getArtifact().getArtifactId(),
                pom.getArtifact().getClassifier(),
                pom.getArtifact().getType(),
                pom.getArtifact().getVersion(),
                null,
                new File(pom.getModel().getBuild().getOutputDirectory()));
    }

    private List<Artifact> getSiblingModules(MavenProject module)
    {
        if (!module.hasParent() || module.getParentFile() == null) {
            return Collections.emptyList();
        }

        // Parent exists and is a project reactor
        MavenProject parent = module.getParent();
        String parentDir = module.getParentFile().getParent();

        return parent.getModules().stream()
                .map(moduleName -> new File(parentDir, moduleName + "/pom.xml"))
                .filter(File::isFile)
                .map(this::getMavenProject)
                .map(this::getProjectArtifact)
                .collect(Collectors.toList());
    }

    /**
     * Returns a string identifying artifact by its maven coordinates.
     */
    private static String getArtifactKey(Artifact artifact)
    {
        return format("%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getClassifier());
    }

    private Properties requiredSystemProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("java.version", System.getProperty("java.version"));
        return properties;
    }

    private Dependency toAetherDependency(org.apache.maven.model.Dependency dependency)
    {
        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion());
        List<Exclusion> exclusions = new ArrayList<>();
        for (org.apache.maven.model.Exclusion exclusion : dependency.getExclusions()) {
            exclusions.add(new Exclusion(exclusion.getGroupId(), exclusion.getArtifactId(), null, "*"));
        }
        return new Dependency(artifact, dependency.getScope(), dependency.isOptional(), Collections.unmodifiableList(exclusions));
    }

    private List<Artifact> resolveArtifacts(DependencyRequest dependencyRequest)
    {
        DependencyResult dependencyResult;
        try {
            dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        }
        catch (DependencyResolutionException e) {
            dependencyResult = e.getResult();
        }
        List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
        List<Artifact> artifacts = new ArrayList<>(artifactResults.size());
        for (ArtifactResult artifactResult : artifactResults) {
            if (artifactResult.isMissing()) {
                artifacts.add(artifactResult.getRequest().getArtifact());
            }
            else {
                artifacts.add(artifactResult.getArtifact());
            }
        }

        return Collections.unmodifiableList(artifacts);
    }

    private static PlexusContainer container()
    {
        try {
            ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());

            ContainerConfiguration cc = new DefaultContainerConfiguration()
                    .setClassWorld(classWorld)
                    .setRealm(null)
                    .setName("maven");

            DefaultPlexusContainer container = new DefaultPlexusContainer(cc);

            // NOTE: To avoid inconsistencies, we'll use the Thread context class loader exclusively for lookups
            container.setLookupRealm(null);

            container.setLoggerManager(new Slf4jLoggerManager());
            container.getLoggerManager().setThresholds(LOGGER_LEVEL_INFO);

            return container;
        }
        catch (PlexusContainerException e) {
            throw new RuntimeException("Error loading Maven system", e);
        }
    }
}
