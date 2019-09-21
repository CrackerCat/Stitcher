package me.jellysquid.stitcher;

import me.jellysquid.stitcher.environment.Environment;
import me.jellysquid.stitcher.environment.EnvironmentPatcher;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.plugin.Plugin;
import me.jellysquid.stitcher.plugin.PluginLoader;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stitcher {
    public static final Logger LOGGER = LogManager.getLogger("Stitcher");

    private static Stitcher instance;

    private final Environment environment;

    private final EnvironmentPatcher patcher;

    private List<Plugin> plugins = Collections.emptyList();

    private Stitcher(Environment environment) {
        this.environment = environment;
        this.patcher = new EnvironmentPatcher(this.environment, StitcherEnvironment.isDebuggingEnabled());
    }

    /**
     * Initializes the global {@link Stitcher} instance. If an instance has already been created, an exception
     * will be thrown.
     */
    public static Stitcher init(Environment environment) {
        if (instance != null) {
            throw new IllegalStateException("Stitcher is already initialized!");
        }

        instance = new Stitcher(environment);
        instance.setup();

        return instance;
    }

    /**
     * @return The current global instance of {@link Stitcher}. If {@link Stitcher#init(Environment)} has not been
     * called yet, this method will throw an {@link IllegalStateException}.
     */
    public static Stitcher instance() {
        if (instance == null) {
            throw new IllegalStateException("Global instance is not initialized yet");
        }

        return instance;
    }

    /**
     * Performs plugin discovery and initializes any found plugins. This logic has been moved outside of the constructor
     * as {@link Stitcher#init(Environment)} may be called during construction but before setup.
     */
    private void setup() {
        this.setupPlugins();
        this.setupTransformers();
    }

    private void setupPlugins() {
        LOGGER.info("Searching for plugins to load");

        this.plugins = PluginLoader.loadAll(this.environment);

        LOGGER.info("Loaded {} plugin(s)", this.plugins.size());
    }

    private void setupTransformers() {
        for (Plugin plugin : this.plugins) {
            try {
                this.loadPlugin(plugin);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Could not load plugin: %s", plugin), e);
            }
        }

        LOGGER.info("Loaded {} class patchers successfully", this.patcher.getRegisteredPatchers().size());
    }

    private void loadPlugin(Plugin plugin) {
        List<ClassTransformer> patchers = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (PluginGroupConfig group : plugin.getGroups()) {
            if (!StitcherEnvironment.DIST.applies(group.getDist())) {
                LOGGER.debug("Disabled group {} from plugin {} as it applies to {} and we are on {}", group.getName(), plugin, group.getDist(), StitcherEnvironment.DIST);
                continue;
            }

            patchers.addAll(this.patcher.registerTransformerGroup(plugin, group));
        }

        LOGGER.debug("Loaded {} transformers from plugin {} in {}ms", patchers.size(), plugin, System.currentTimeMillis() - start);
    }

    public EnvironmentPatcher getPatcher() {
        return this.patcher;
    }
}
