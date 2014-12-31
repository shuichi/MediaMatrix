package mediamatrix.classloader;


import java.io.File;
import java.io.FilenameFilter;
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
        jarFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String[] fileSuffixes = new String[]{".jar", ".JAR", ".zip", ".ZIP"};
                File file = new File(dir, name);
                boolean flag = false;
                if (file.isFile()) {
                    for (int i = 0; i < fileSuffixes.length; i++) {
                        if (name.endsWith(fileSuffixes[i])) {
                            flag = true;
                            break;
                        }
                    }
                } else {
                    flag = false;
                }
                return flag;
            }
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
        final List<Class<?>> list = new ArrayList<Class<?>>();
        for (int i = 0; i < pluginClasses.length; i++) {
            if (ClassUtilities.isSubclassOf(pluginClasses[i], baseClass)) {
                list.add(pluginClasses[i]);
            }
        }
        return list.toArray(new Class<?>[list.size()]);
    }
    
    
    
    private Class<?>[] findPlugins() throws IOException {
        List<Class<?>> list = new ArrayList<Class<?>>();
        for (int i = 0; i < jarFiles.length; i++) {
            if (jarFiles[i].getName().startsWith("lib-")) {
                continue;
            }
            
            JarFile jar = new JarFile(jarFiles[i]);
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
        return list.toArray(new Class<?>[list.size()]);
    }
    
    
    
    private boolean hasTargetSuffix(String className) {
        for (int i = 0; i < suffixes.length; i++) {
            if (className.endsWith(suffixes[i])) {
                return true;
            }
        }
        return false;
    }
    
    
    
    
    private boolean isPluginClass(Class<?> targetClass) {
        for (int i = 0; i < baseClasses.length; i++) {
            if (ClassUtilities.isSubclassOf(targetClass, baseClasses[i])) {
                return true;
            }
        }
        return false;
    }
      
    
}
