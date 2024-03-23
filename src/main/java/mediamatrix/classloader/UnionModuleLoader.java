package mediamatrix.classloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnionModuleLoader implements DynamicModuleLoader {

    private final DynamicModuleLoader[] loaders;

    public UnionModuleLoader(DynamicModuleLoader[] loaders) {
        this.loaders = loaders;
    }

    @Override
    public Class<?>[] getPlugins(Class<?> baseClass) {
        final List<Class<?>> result = new ArrayList<>();
        for (DynamicModuleLoader loader : loaders) {
            Class<?>[] modules = loader.getPlugins(baseClass);
            result.addAll(Arrays.asList(modules));
        }
        return result.toArray(Class<?>[]::new);
    }

    @Override
    public Class<?>[] getPlugins() {
        final List<Class<?>> result = new ArrayList<>();
        for (DynamicModuleLoader loader : loaders) {
            Class<?>[] modules = loader.getPlugins();
            result.addAll(Arrays.asList(modules));
        }
        return result.toArray(Class<?>[]::new);
    }

}
