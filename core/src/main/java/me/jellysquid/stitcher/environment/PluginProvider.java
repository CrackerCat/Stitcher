package me.jellysquid.stitcher.environment;

import me.jellysquid.stitcher.plugin.PluginCandidate;

import java.io.IOException;
import java.util.stream.Stream;

public interface PluginProvider {
    /**
     * Discovers all possible candidate plugins for this environment.
     *
     * @return A stream of plugins which can be considered for loading
     * @throws IOException If an I/O error occurs while attempting to discover plugins
     */
    Stream<PluginCandidate> discoverCandidatePlugins() throws IOException;
}
