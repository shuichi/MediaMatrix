package mediamatrix.classloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnionModuleLoader implements DynamicModuleLoader {
    
    private DynamicModuleLoader[] loaders;
    
    public UnionModuleLoader(DynamicModuleLoader[] loaders) {
        this.loaders = loaders;
    }

    
    @Override
    public Class<?>[] getPlugins(Class<?> baseClass) {
        final List<Class<?>> result = new ArrayList<Class<?>>();
        for (int i = 0; i < loaders.length; i++) {
            Class<?>[] modules = loaders[i].getPlugins(baseClass);
            result.addAll(Arrays.asList(modules));
        }
        return result.toArray(new Class<?>[result.size()]);
    }
    
    
    
    @Override
    public Class<?>[] getPlugins() {
        final List<Class<?>> result = new ArrayList<Class<?>>();
        for (int i = 0; i < loaders.length; i++) {
            Class<?>[] modules = loaders[i].getPlugins();
            result.addAll(Arrays.asList(modules));
        }
        return result.toArray(new Class<?>[result.size()]);
    }
    
}
