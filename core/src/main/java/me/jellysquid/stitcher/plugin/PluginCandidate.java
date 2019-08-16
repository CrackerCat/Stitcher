package me.jellysquid.stitcher.plugin;

public class PluginCandidate {
    private final PluginManifest manifest;

    private final PluginResourceLoader resources;

    public PluginCandidate(PluginManifest manifest, PluginResourceLoader resources) {
        this.manifest = manifest;
        this.resources = resources;
    }

    public PluginManifest getManifest() {
        return this.manifest;
    }

    public PluginResourceLoader getResources() {
        return this.resources;
    }

    @Override
    public String toString() {
        return String.format("PluginCandidate{manifest=%s, resource=%s", this.manifest, this.resources);
    }
}
