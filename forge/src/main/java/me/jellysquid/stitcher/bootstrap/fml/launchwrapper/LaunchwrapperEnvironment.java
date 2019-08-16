package me.jellysquid.stitcher.bootstrap.fml.launchwrapper;

import me.jellysquid.stitcher.StitcherEnvironment;
import me.jellysquid.stitcher.environment.Environment;
import me.jellysquid.stitcher.plugin.PluginCandidate;
import me.jellysquid.stitcher.plugin.PluginManifest;
import me.jellysquid.stitcher.plugin.PluginResourceLoader;
import me.jellysquid.stitcher.plugin.resources.ClassLoaderResourceLoader;
import me.jellysquid.stitcher.plugin.resources.ZipFileResourceLoader;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class LaunchwrapperEnvironment implements Environment {
    private final LaunchwrapperHelper helper = new LaunchwrapperHelper(Launch.classLoader);

    private final Path home;

    public LaunchwrapperEnvironment() {
        if (Launch.minecraftHome == null) {
            this.home = Paths.get(".");
        } else {
            this.home = Launch.minecraftHome.toPath();
        }
    }

    @Override
    public Stream<PluginCandidate> discoverCandidatePlugins() throws IOException {
        Stream<PluginResourceLoader> jars = Files.walk(this.home.resolve("mods"))
                .filter(path -> path.endsWith(".jar"))
                .map(this::openPluginFromJAR);

        Stream<PluginResourceLoader> classpath = StitcherEnvironment.getCommandLinePlugins().stream()
                .map(this::openPluginFromClasspath);

        return Stream.concat(jars, classpath)
                .map(LaunchwrapperEnvironment::createCandidate)
                .filter(Objects::nonNull);
    }

    @Override
    public boolean isClassLoaded(String className) {
        return this.helper.isClassLoaded(className);
    }

    @Override
    public Path getHomeDirectory() {
        return this.home;
    }

    private static PluginCandidate createCandidate(PluginResourceLoader resources) {
        PluginManifest manifest = resources.getPluginManifest();

        if (manifest == null) {
            return null;
        }

        return new PluginCandidate(manifest, resources);
    }

    private PluginResourceLoader openPluginFromJAR(Path path) {
        try {
            return new ZipFileResourceLoader(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize resource loader", e);
        }
    }

    private PluginResourceLoader openPluginFromClasspath(String name) {
        return new ClassLoaderResourceLoader(new PluginManifest(name), LaunchwrapperEnvironment.class.getClassLoader());
    }
}
