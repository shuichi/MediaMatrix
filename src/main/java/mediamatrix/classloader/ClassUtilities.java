package mediamatrix.classloader;

import mediamatrix.utils.FileNameUtilities;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class ClassUtilities {

    public ClassUtilities() {
    }

    public static String pathToFullyQualifiedName(String classpath, String path) {
        char[] buff = new char[path.length()];
        String fqname;

        for (int i = 0; i < buff.length; i++) {
            if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
                buff[i] = '.';
            } else {
                buff[i] = path.charAt(i);
            }
        }

        int classpathLength = 0;
        if (classpath != null) {
            classpathLength = classpath.length() + 1;
        }
        int classNameLength = path.length() - classpathLength - new String(".class").length();
        fqname = new String(buff, classpathLength, classNameLength);
        return fqname;
    }

    public static boolean isSubclassOf(Class<?> targetClass, Class<?> baseClass) {
        for (Class<?> clazz = targetClass; clazz != null; clazz = clazz.getSuperclass()) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> intf : interfaces) {
                if (intf.equals(baseClass)) {
                    return true;
                }
            }
            if (clazz.equals(baseClass) && !clazz.equals(targetClass)) {
                return true;
            }
        }

        return false;
    }

    public static URL[] convertJarFilesToURLs(File[] jars) throws IOException {
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            urls[i] = URI.create("jar:" + FileNameUtilities.toURL(jars[i].getCanonicalFile()).toString() + "!/").toURL();
        }
        return urls;
    }
}
