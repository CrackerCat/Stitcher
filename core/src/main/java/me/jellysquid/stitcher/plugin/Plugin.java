package me.jellysquid.stitcher.plugin;

import me.jellysquid.stitcher.plugin.config.PluginConfig;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;

import java.util.List;

public class Plugin {
    private final PluginResourceProvider resources;

    private final PluginConfig pluginConfig;

    private final List<PluginGroupConfig> groups;

    public Plugin(PluginResourceProvider resources, PluginConfig pluginConfig, List<PluginGroupConfig> groups) {
        this.resources = resources;
        this.pluginConfig = pluginConfig;
        this.groups = groups;
    }

    public PluginConfig getConfig() {
        return this.pluginConfig;
    }

    public List<PluginGroupConfig> getGroups() {
        return this.groups;
    }

    public PluginResourceProvider getResources() {
        return this.resources;
    }

    @Override
    public String toString() {
        return String.format("Plugin{name=%s,src=%s}", this.pluginConfig.getName(), this.resources.getSource());
    }
}
