package mediamatrix.classloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class ReflectiveModuleLoader implements DynamicModuleLoader {

    private final FileClassLoader classLoader;
    private final String[] suffixes;
    private final Class<?>[] baseClasses;
    private final File[] jarFiles;
    private final Class<?>[] pluginClasses;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ReflectiveModuleLoader(File dir, String[] suffixes, Class<?>[] baseClasses) throws IOException {
        this.suffixes = suffixes;
        this.baseClasses = baseClasses;
        jarFiles = dir.listFiles((File dir1, String name) -> {
            String[] fileSuffixes = new String[]{".jar", ".JAR", ".zip", ".ZIP"};
            File file = new File(dir1, name);
            boolean flag = false;
            if (file.isFile()) {
                for (String fileSuffixe : fileSuffixes) {
                    if (name.endsWith(fileSuffixe)) {
                        flag = true;
                        break;
                    }
                }
            } else {
                flag = false;
            }
            return flag;
        });
        classLoader = new FileClassLoader(jarFiles, getClass().getClassLoader());
        pluginClasses = findPlugins();
    }

    @Override
    public Class<?>[] getPlugins() {
        return pluginClasses;
    }

    @Override
    public Class<?>[] getPlugins(Class<?> baseClass) {
        final List<Class<?>> list = new ArrayList<>();
        for (Class<?> pluginClasse : pluginClasses) {
            if (ClassUtilities.isSubclassOf(pluginClasse, baseClass)) {
                list.add(pluginClasse);
            }
        }
        return list.toArray(Class<?>[]::new);
    }

    private Class<?>[] findPlugins() throws IOException {
        List<Class<?>> list = new ArrayList<>();
        for (File jarFile : jarFiles) {
            if (jarFile.getName().startsWith("lib-")) {
                continue;
            }
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                String className = ClassUtilities.pathToFullyQualifiedName(null, entry.getName());
                if (hasTargetSuffix(className)) {
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (isPluginClass(clazz)) {
                            list.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.throwing(getClass().getName(), "findPlugins()", e);
                    }
                }
            }
        }
        return list.toArray(Class<?>[]::new);
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
