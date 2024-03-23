package mediamatrix.classloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileClassLoader extends ClassLoader {

    private final HashMap<String, Class<?>> cache = new HashMap<>();
    private File[] jarFiles;

    public FileClassLoader() {
        super();
    }

    public FileClassLoader(ClassLoader parent) {
        super(parent);
    }

    public FileClassLoader(File[] jars, ClassLoader parent) {
        super(parent);
        init(jars);
    }

    @Override
    public URL getResource(String name) {
        for (File jarFile : jarFiles) {
            if (jarFile != null) {
                URL url = loadResourceFromJarFile(jarFile, name);
                if (url != null) {
                    return url;
                }
            }
        }

        return getSystemResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (File jarFile : jarFiles) {
            if (jarFile != null) {
                InputStream in = loadStreamFromJarFile(jarFile, name);
                if (in != null) {
                    return in;
                }
            }
        }
        return getSystemResourceAsStream(name);
    }

    public synchronized void init(File[] jars) {
        jarFiles = new File[jars.length];
        for (int i = 0; i < jars.length; i++) {
            if (!jars[i].exists()) {
                throw new IllegalArgumentException("ClassPath " + jars[i] + " does not exist");
            }
            if (!jars[i].canRead()) {
                throw new IllegalArgumentException("ClassPath " + jars[i] + " cannot be read");
            }
            if (jars[i].isDirectory()) {
                throw new IllegalArgumentException("ClassPath " + jars[i] + " cannot be directory");
            }

            ZipFile jarFile = null;
            try {
                jarFile = new ZipFile(jars[i]);
                Enumeration<? extends ZipEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    InputStream is = jarFile.getInputStream(entry);
                    is.close();
                    break;
                }
            } catch (IOException t) {
                throw new IllegalArgumentException("Cannot read JAR file " + jars[i]);
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException ex) {
                    }
                }
            }
            jarFiles[i] = jars[i];
        }
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> theClass;
        theClass = cache.get(name);

        if (theClass == null) {
            for (File jarFile : jarFiles) {
                theClass = loadClassFromJarFile(jarFile, name);
                if (theClass != null) {
                    break;
                }
            }
        }

        if (theClass == null) {
            theClass = getParent().loadClass(name);
        }

        if (theClass != null && resolve) {
            resolveClass(theClass);
            cache.put(name, theClass);
        }

        if (theClass == null) {
            throw new ClassNotFoundException(name);
        }

        return theClass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FileClassLoader[");
        for (int i = 0; i < jarFiles.length; i++) {
            if (i > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(jarFiles[i]);
        }
        sb.append("]");
        return (sb.toString());
    }

    private Class<?> loadClassFromJarFile(File jar, String name) {
        final String filename = name.replace('.', '/') + ".class";
        ZipFile jarFile = null;
        ZipEntry jarEntry;
        Class<?> theClass = null;
        InputStream is = null;

        try {
            jarFile = new ZipFile(jar);
            jarEntry = jarFile.getEntry(filename);
            if (jarEntry != null) {
                byte[] buffer = new byte[(int) jarEntry.getSize()];
                is = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                is.read(buffer);
                theClass = defineClass(name, buffer, 0, buffer.length);
            }
        } catch (IOException e) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                }
            }
        }

        return theClass;
    }

    private URL loadResourceFromJarFile(File jar, String name) {
        String filename = name;

        ZipFile jarFile = null;
        ZipEntry jarEntry;
        URL url = null;

        try {
            jarFile = new ZipFile(jar);
            jarEntry = jarFile.getEntry(filename);
            if (jarEntry != null) {
                try {
                    url = URI.create("jar:file:" + jar.getAbsolutePath() + "!/" + name).toURL();
                } catch (MalformedURLException e) {
                }
            }
        } catch (IOException e) {
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                }
            }
        }

        return url;
    }

    private InputStream loadStreamFromJarFile(File jar, String name) {
        String filename = name;
        ZipFile jarFile = null;
        ZipEntry jarEntry;
        InputStream is = null;
        try {
            jarFile = new ZipFile(jar);
            jarEntry = jarFile.getEntry(filename);
            if (jarEntry != null) {
                is = new BufferedInputStream(jarFile.getInputStream(jarEntry));
            }
        } catch (IOException e) {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                }
            }
        }
        return is;
    }
}
