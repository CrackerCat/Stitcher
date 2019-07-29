package me.jellysquid.stitcher.bootstrap.fml.launchwrapper;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.Map;

public class LaunchwrapperHelper {
    private final Map<String, Class<?>> cachedClasses;

    LaunchwrapperHelper(LaunchClassLoader classLoader) {
        this.cachedClasses = LaunchwrapperHelper.getFieldValue(classLoader, "cachedClasses");
    }

    public boolean isClassLoaded(String name) {
        return this.cachedClasses.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(LaunchClassLoader classLoader, String fieldName) {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            return (T) field.get(classLoader);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not access field value using reflection", e);
        }
    }
}
