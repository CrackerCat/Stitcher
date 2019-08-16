package me.jellysquid.stitcher.plugin;

import com.eclipsesource.json.Json;
import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.environment.PluginProvider;
import me.jellysquid.stitcher.plugin.config.PluginConfig;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginLoader {
    /**
     * Creates a {@link Plugin} for every available and valid plugin discovered by {@param environment}.
     *
     * @param provider The {@link PluginProvider} which will discover the plugins for the factory to process
     * @return A list of instances representing valid plugins
     */
    public static List<Plugin> loadAll(PluginProvider provider) {
        try {
            return provider.discoverCandidatePlugins()
                    .map(PluginLoader::loadPlugin)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Stitcher.LOGGER.warn("Could not locate plugins", e);
        }

        return new ArrayList<>();
    }

    private static Plugin loadPlugin(PluginCandidate candidate) {
        Stitcher.LOGGER.debug("Considering plugin candidate {}", candidate);

        PluginManifest manifest = candidate.getManifest();
        PluginResourceLoader resources = candidate.getResources();

        String configName = "stitcher." + manifest.getName() + ".json";

        if (!resources.exists(configName)) {
            Stitcher.LOGGER.warn("Plugin configuration file '{}' is missing from plugin resources (plugin: {})", configName, candidate);

            return null;
        }

        Plugin plugin;

        try (Reader stream = new InputStreamReader(resources.getStream(configName))) {
            PluginConfig pluginConfig = new PluginConfig(manifest, Json.parse(stream).asObject());

            List<PluginGroupConfig> groups = new ArrayList<>();

            for (String module : pluginConfig.getModules()) {
                groups.add(loadTransformerConfig(resources, pluginConfig, module));
            }

            plugin = new Plugin(resources, pluginConfig, groups);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load plugin config", e);
        }

        Stitcher.LOGGER.debug("Loaded plugin from candidate {}", candidate);

        return plugin;
    }

    private static PluginGroupConfig loadTransformerConfig(PluginResourceLoader resources, PluginConfig pluginConfig, String group) {
        String path = "stitcher." + pluginConfig.getName() + "." + group + ".json";

        try (Reader reader = new InputStreamReader(resources.getStream(path))) {
            return new PluginGroupConfig(group, Json.parse(reader).asObject());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load plugin module config", e);
        }
    }
}
