package me.jellysquid.stitcher.environment;

import me.jellysquid.stitcher.plugin.PluginCandidate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface Environment {
    /**
     * Discovers all possible candidate plugins for this environment.
     *
     * @return A stream of plugins which can be considered for loading
     * @throws IOException If an I/O error occurs while attempting to discover plugins
     */
    Stream<PluginCandidate> discoverCandidatePlugins() throws IOException;

    /**
     * Returns whether or not a class has already been loaded or transformed in this environment.
     *
     * @return True if the class has been transformed, otherwise false
     */
    boolean isClassLoaded(String className);

    Path getHomeDirectory();
}
