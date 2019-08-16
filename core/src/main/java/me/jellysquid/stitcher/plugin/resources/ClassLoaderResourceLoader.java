package me.jellysquid.stitcher.plugin.resources;

import me.jellysquid.stitcher.plugin.PluginManifest;
import me.jellysquid.stitcher.plugin.PluginResourceLoader;
import me.jellysquid.stitcher.util.StreamHelper;

import java.io.IOException;
import java.io.InputStream;

public class ClassLoaderResourceLoader implements PluginResourceLoader {
    private final PluginManifest manifest;

    private final ClassLoader classLoader;

    public ClassLoaderResourceLoader(PluginManifest manifest, ClassLoader classLoader) {
        this.manifest = manifest;
        this.classLoader = classLoader;
    }

    @Override
    public PluginManifest getPluginManifest() {
        return this.manifest;
    }

    @Override
    public byte[] getBytes(String name) throws IOException {
        try (InputStream in = this.getStream(name)) {
            return StreamHelper.toByteArray(in);
        }
    }

    @Override
    public InputStream getStream(String name) throws IOException {
        InputStream stream = this.classLoader.getResourceAsStream(name);

        if (stream == null) {
            throw new IOException("Could not find resource: " + name);
        }

        return stream;
    }

    @Override
    public boolean exists(String name) {
        return this.classLoader.getResource(name) != null;
    }

    @Override
    public String getSource() {
        return this.classLoader.getClass().getName();
    }

    @Override
    public String toString() {
        return String.format("ClassLoaderPlugin{name=%s, classLoader=%s}", this.manifest.getName(), this.classLoader);
    }
}
