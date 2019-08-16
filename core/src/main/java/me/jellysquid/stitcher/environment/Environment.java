package me.jellysquid.stitcher.environment;

import java.nio.file.Path;

public interface Environment extends PluginProvider {
    /**
     * Returns whether or not a class has already been loaded or transformed in this environment.
     *
     * @return True if the class has been transformed, otherwise false
     */
    boolean isClassLoaded(String className);

    Path getHomeDirectory();
}
