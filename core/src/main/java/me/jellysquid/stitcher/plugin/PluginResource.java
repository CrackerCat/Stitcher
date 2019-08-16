package me.jellysquid.stitcher.plugin;

import java.io.IOException;
import java.io.InputStream;

public class PluginResource {
    private final Plugin plugin;

    private final String path;

    public PluginResource(Plugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public byte[] getBytes() throws IOException {
        return this.plugin.getLoader().getBytes(this.path);
    }

    public InputStream getStream() throws IOException {
        return this.plugin.getLoader().getStream(this.path);
    }
}
