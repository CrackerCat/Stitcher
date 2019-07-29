package me.jellysquid.stitcher;

import me.jellysquid.stitcher.environment.Environment;
import me.jellysquid.stitcher.environment.EnvironmentPatcher;
import me.jellysquid.stitcher.patcher.ClassPatcher;
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

    private Stitcher(Environment env) {
        this.environment = env;

        this.patcher = new EnvironmentPatcher(this.environment, StitcherEnvironment.isDebuggingEnabled());
    }

    /**
     * @return The global instance of {@link Stitcher}.
     * @throws IllegalStateException If an instance of {@link Stitcher} has not yet been initialized
     */
    public static Stitcher instance() {
        if (instance == null) {
            throw new IllegalStateException("Stitcher instance not initialized yet");
        }

        return instance;
    }

    /**
     * Initializes the global {@link Stitcher} instance. If an instance has already been created, it will instead return
     * the current instance.
     */
    public static Stitcher init(Environment env) {
        if (instance == null) {
            instance = new Stitcher(env);
            instance.setup();
        }

        return instance;
    }

    /**
     * Performs plugin discovery and initializes any found plugins. This logic has been moved outside of the constructor
     * as {@link Stitcher#instance()} may be called during construction but before setup.
     */
    private void setup() {
        this.environment.setup(this);

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
                throw new RuntimeException("Could not load plugin: " + plugin, e);
            }
        }

        LOGGER.info("Loaded {} class patchers successfully", this.patcher.getRegisteredPatchers().size());
    }

    private void loadPlugin(Plugin plugin) {
        List<ClassPatcher> patchers = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (PluginGroupConfig group : plugin.getGroups()) {
            if (!StitcherEnvironment.DIST.applies(group.getDist())) {
                LOGGER.debug("Disabled group {} from plugin {} as it applies to {} and we are on {}", group.getName(), plugin, group.getDist(), StitcherEnvironment.DIST);
                continue;
            }

            patchers.addAll(this.patcher.registerTransformerGroup(plugin, group));
        }

        LOGGER.debug("Loaded {} class patchers from plugin {} in {}ms", patchers.size(), plugin, System.currentTimeMillis() - start);
    }

    public EnvironmentPatcher getPatcher() {
        return this.patcher;
    }
}
