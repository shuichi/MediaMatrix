package mediamatrix.classloader;

public interface DynamicModuleLoader {
    Class<?>[] getPlugins();
    Class<?>[] getPlugins(Class<?> baseClass);
}
