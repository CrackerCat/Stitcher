package me.jellysquid.stitcher.plugin;

import me.jellysquid.stitcher.plugin.config.PluginConfig;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;

import java.util.List;

public class Plugin {
    private final PluginResourceLoader loader;

    private final PluginConfig pluginConfig;

    private final List<PluginGroupConfig> groups;

    public Plugin(PluginResourceLoader loader, PluginConfig pluginConfig, List<PluginGroupConfig> groups) {
        this.loader = loader;
        this.pluginConfig = pluginConfig;
        this.groups = groups;
    }

    public PluginConfig getConfig() {
        return this.pluginConfig;
    }

    public List<PluginGroupConfig> getGroups() {
        return this.groups;
    }

    public PluginResourceLoader getLoader() {
        return this.loader;
    }

    public PluginResource getResource(String name) {
        return new PluginResource(this, name);
    }

    @Override
    public String toString() {
        return String.format("Plugin{name=%s,src=%s}", this.pluginConfig.getName(), this.loader.getSource());
    }
}
