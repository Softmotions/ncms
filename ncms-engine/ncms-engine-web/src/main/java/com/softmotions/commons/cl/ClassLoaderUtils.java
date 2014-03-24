package com.softmotions.commons.cl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 */
public class ClassLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    /**
     * Taken from: http://tomee.apache.org/
     * <p>
     * Destroy a classloader as forcefully as possible.
     *
     * @param classLoader ClassLoader to destroy.
     */
    public static void destroyClassLoader(final ClassLoader classLoader) {
        //Clear open jar files belonging to this ClassLoader
        for (final String jar : getClosedJarFiles(classLoader)) {
            clearSunJarFileFactoryCache(jar);
        }
    }

    /**
     * Taken from: http://tomee.apache.org/
     * <p>
     * Dirty hack to force closure of file handles in the Oracle VM URLClassLoader
     * Any URLClassLoader passed into this method will be unusable after the method completes.
     *
     * @param cl ClassLoader of expected type URLClassLoader (Silent failure)
     */
    private static List<String> getClosedJarFiles(final ClassLoader cl) {

        final List<String> files = new ArrayList<String>();

        if (null != cl && cl instanceof URLClassLoader) {

            final URLClassLoader ucl = (URLClassLoader) cl;
            final Class clazz = URLClassLoader.class;

            try {

                final Field ucp = clazz.getDeclaredField("ucp");
                ucp.setAccessible(true);
                final Object cp = ucp.get(ucl);
                final Field loaders = cp.getClass().getDeclaredField("loaders");
                loaders.setAccessible(true);
                final Collection c = (Collection) loaders.get(cp);
                Field loader;
                JarFile jf;

                for (final Object jl : c.toArray()) {
                    try {
                        loader = jl.getClass().getDeclaredField("jar");
                        loader.setAccessible(true);
                        jf = (JarFile) loader.get(jl);
                        files.add(jf.getName());
                        jf.close();
                    } catch (Throwable t) {
                        //If we got this far, this is probably not a JAR loader so skip it
                    }
                }
            } catch (Throwable t) {
                //Not an Oracle VM
            }
        }
        return files;
    }


    public static void clearSunJarFileFactoryCache(final String jarLocation) {
        clearSunJarFileFactoryCacheImpl(jarLocation, 5);
    }

    /**
     * Due to several different implementation changes in various JDK releases the code here is not as
     * straight forward as reflecting debug items in your current runtime. There have even been breaking changes
     * between 1.6 runtime builds, let alone 1.5.
     * <p>
     * If you discover a new issue here please be careful to ensure the existing functionality is 'extended' and not
     * just replaced to match your runtime observations.
     * <p>
     * If you want to look at the mess that leads up to this then follow the source code changes made to
     * the class sun.net.www.protocol.jar.JarFileFactory over several years.
     *
     * @param jarLocation String
     * @param attempt     int
     */
    @SuppressWarnings({"unchecked"})
    private static synchronized void clearSunJarFileFactoryCacheImpl(final String jarLocation, final int attempt) {
        logger.debug("Clearing Sun JarFileFactory cache for directory " + jarLocation);

        try {
            final Class jarFileFactory = Class.forName("sun.net.www.protocol.jar.JarFileFactory");

            //Do not generify these maps as their contents are NOT stable across runtimes.
            final Field fileCacheField = jarFileFactory.getDeclaredField("fileCache");
            fileCacheField.setAccessible(true);
            final Map fileCache = (Map) fileCacheField.get(null);
            final Map fileCacheCopy = new HashMap(fileCache);

            final Field urlCacheField = jarFileFactory.getDeclaredField("urlCache");
            urlCacheField.setAccessible(true);
            final Map urlCache = (Map) urlCacheField.get(null);
            final Map urlCacheCopy = new HashMap(urlCache);

            //The only stable item we have here is the JarFile/ZipFile in this map
            Iterator iterator = urlCacheCopy.entrySet().iterator();
            final List urlCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object key = entry.getKey();

                if (key instanceof ZipFile) {
                    final ZipFile zf = (ZipFile) key;
                    final File file = new File(zf.getName());  //getName returns File.getPath()
                    if (isParent(jarLocation, file)) {
                        //Flag for removal
                        urlCacheRemoveKeys.add(key);
                    }
                } else {
                    logger.warn("Unexpected key type: " + key);
                }
            }

            iterator = fileCacheCopy.entrySet().iterator();
            final List fileCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object value = entry.getValue();

                if (urlCacheRemoveKeys.contains(value)) {
                    fileCacheRemoveKeys.add(entry.getKey());
                }
            }

            //Use these unstable values as the keys for the fileCache values.
            iterator = fileCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = fileCache.remove(next);
                    if (null != remove) {
                        logger.debug("Removed item from fileCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warn("Failed to remove item from fileCache: " + next);
                }
            }

            iterator = urlCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = urlCache.remove(next);
                    try {
                        ((AutoCloseable) next).close();
                    } catch (Throwable e) {
                        //Ignore
                    }

                    if (null != remove) {
                        logger.debug("Removed item from urlCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warn("Failed to remove item from urlCache: " + next);
                }
            }
        } catch (ConcurrentModificationException e) {
            if (attempt > 0) {
                clearSunJarFileFactoryCacheImpl(jarLocation, (attempt - 1));
            } else {
                logger.error("Unable to clear Sun JarFileFactory cache after 5 attempts", e);
            }
        } catch (ClassNotFoundException e) {
            // not a sun vm
        } catch (NoSuchFieldException e) {
            // different version of sun vm?
        } catch (Throwable e) {
            logger.error("Unable to clear Sun JarFileFactory cache", e);
        }
    }


    private static boolean isParent(final String jarLocation, File file) {
        final File dir = new File(jarLocation);
        while (file != null) {
            if (file.equals(dir)) {
                return true;
            }
            file = file.getParentFile();
        }
        return false;
    }
}
