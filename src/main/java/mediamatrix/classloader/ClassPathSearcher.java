package mediamatrix.classloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class ClassPathSearcher implements DynamicModuleLoader {

    private final String[] suffixes;
    private final Class<?>[] baseClasses;
    private final Set<Class<?>> pluginClasses;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ClassPathSearcher(String[] suffixes, Class<?>[] baseClasses) throws IOException {
        this.suffixes = suffixes;
        this.baseClasses = baseClasses;
        this.pluginClasses = new HashSet<>();
        searchClassPath();
    }

    @Override
    public Class<?>[] getPlugins() {
        return pluginClasses.toArray(Class<?>[]::new);
    }

    @Override
    public Class<?>[] getPlugins(Class<?> baseClass) {
        final List<Class<?>> list = new ArrayList<>();
        for (Class<?> clazz : pluginClasses) {
            if (ClassUtilities.isSubclassOf(clazz, baseClass)) {
                list.add(clazz);
            }
        }
        return list.toArray(Class<?>[]::new);
    }

    private void searchClassPath() throws IOException {
        String line = System.getProperty("java.class.path");
        String items[] = line.split(System.getProperty("path.separator"));
        for (String item : items) {
            File file = new File(item);
            if (file.isDirectory()) {
                recursiveDirectory(item, file);
            }
            if (file.toString().endsWith(".jar")) {
                searchJar(file);
            }
        }
    }

    private void recursiveDirectory(String classpath, File dir) {
        final File[] contents = dir.listFiles();
        for (File content : contents) {
            if (content.toString().endsWith(".class")) {
                final String className = ClassUtilities.pathToFullyQualifiedName(classpath, content.toString());
                if (hasTargetSuffix(className)) {
                    try {
                        final Class<?> clazz = Class.forName(className);
                        if (isPluginClass(clazz)) {
                            pluginClasses.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.throwing(getClass().getName(), "recursiveDirectory()", e);
                    }
                }
            }
            if (content.isDirectory()) {
                recursiveDirectory(classpath, content);
            }
        }
    }

    private void searchJar(File file) throws IOException {
        final JarFile jar = new JarFile(file);
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                final String className = ClassUtilities.pathToFullyQualifiedName(null, entry.getName());
                if (hasTargetSuffix(className)) {
                    try {
                        final Class<?> clazz = Class.forName(className);
                        if (isPluginClass(clazz)) {
                            pluginClasses.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.throwing(getClass().getName(), "searchJar()", e);
                    }
                }
            }
        }
    }

    private boolean hasTargetSuffix(String className) {
        for (String suffixe : suffixes) {
            if (className.endsWith(suffixe)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPluginClass(Class<?> targetClass) {
        for (Class<?> baseClasse : baseClasses) {
            if (ClassUtilities.isSubclassOf(targetClass, baseClasse)) {
                return true;
            }
        }
        return false;
    }
}
