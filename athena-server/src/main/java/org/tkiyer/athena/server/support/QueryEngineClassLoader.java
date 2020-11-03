package org.tkiyer.athena.server.support;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Copy from io.prestosql.server.PluginClassLoader
 */
public class QueryEngineClassLoader extends URLClassLoader {

    private static final ClassLoader PLATFORM_CLASS_LOADER = findPlatformClassLoader();

    private final ClassLoader queryEngineApiClassLoader;

    private final List<String> queryEngineApiPackages;

    private final List<String> queryEngineApiResources;

    public QueryEngineClassLoader(
            List<URL> urls,
            ClassLoader queryEngineApiClassLoader,
            List<String> queryEngineApiPackages)
    {
        this(urls,
                queryEngineApiClassLoader,
                queryEngineApiPackages,
                queryEngineApiPackages.stream()
                        .map(QueryEngineClassLoader::classNameToResource)
                        .collect(Collectors.toList()));
    }

    private QueryEngineClassLoader(
            List<URL> urls,
            ClassLoader queryEngineApiClassLoader,
            List<String> queryEngineApiPackages,
            List<String> queryEngineApiResources)
    {
        // plugins should not have access to the system (application) class loader
        super(urls.toArray(new URL[0]), PLATFORM_CLASS_LOADER);
        this.queryEngineApiClassLoader = requireNonNull(queryEngineApiClassLoader, "spiClassLoader is null");
        this.queryEngineApiPackages = Collections.unmodifiableList(queryEngineApiPackages);
        this.queryEngineApiResources = Collections.unmodifiableList(queryEngineApiResources);
    }

    public QueryEngineClassLoader duplicate()
    {
        return new QueryEngineClassLoader(Collections.unmodifiableList(Arrays.asList(getURLs())), queryEngineApiClassLoader, queryEngineApiPackages, queryEngineApiResources);
    }

    public QueryEngineClassLoader withUrl(URL url)
    {
        List<URL> temp = Arrays.asList(getURLs());
        temp.add(url);
        List<URL> urls = Collections.unmodifiableList(temp);
        return new QueryEngineClassLoader(urls, queryEngineApiClassLoader, queryEngineApiPackages, queryEngineApiResources);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        // grab the magic lock
        synchronized (getClassLoadingLock(name)) {
            // Check if class is in the loaded classes cache
            Class<?> cachedClass = findLoadedClass(name);
            if (cachedClass != null) {
                return resolveClass(cachedClass, resolve);
            }

            // If this is an SPI class, only check SPI class loader
            if (isQueryEngineApiClass(name)) {
                return resolveClass(queryEngineApiClassLoader.loadClass(name), resolve);
            }

            // Look for class locally
            return super.loadClass(name, resolve);
        }
    }

    private Class<?> resolveClass(Class<?> clazz, boolean resolve)
    {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public URL getResource(String name)
    {
        // If this is an SPI resource, only check SPI class loader
        if (isQueryEngineApiResource(name)) {
            return queryEngineApiClassLoader.getResource(name);
        }

        // Look for resource locally
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name)
            throws IOException
    {
        // If this is an SPI resource, use SPI resources
        if (isQueryEngineApiClass(name)) {
            return queryEngineApiClassLoader.getResources(name);
        }

        // Use local resources
        return super.getResources(name);
    }

    private boolean isQueryEngineApiClass(String name)
    {
        // todo maybe make this more precise and only match base package
        return queryEngineApiPackages.stream().anyMatch(name::startsWith);
    }

    private boolean isQueryEngineApiResource(String name)
    {
        // todo maybe make this more precise and only match base package
        return queryEngineApiResources.stream().anyMatch(name::startsWith);
    }

    private static String classNameToResource(String className)
    {
        return className.replace('.', '/');
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static ClassLoader findPlatformClassLoader()
    {
        try {
            // use platform class loader on Java 9
            Method method = ClassLoader.class.getMethod("getPlatformClassLoader");
            return (ClassLoader) method.invoke(null);
        }
        catch (NoSuchMethodException ignored) {
            // use null class loader on Java 8
            return null;
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
