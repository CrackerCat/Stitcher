package me.jellysquid.stitcher.plugin;

public class PluginManifest {
    public final String name;

    public PluginManifest(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("PluginManifest{name=%s}", this.name);
    }
}
