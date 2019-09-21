package me.jellysquid.stitcher.plugin.resources;

import me.jellysquid.stitcher.plugin.PluginManifest;
import me.jellysquid.stitcher.plugin.PluginResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileResourceLoader implements PluginResourceLoader {
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String MANIFEST_ATTRIBUTE = "Stitcher-Plugin-Name";

    private final ZipFile file;

    private final PluginManifest manifest;

    public ZipFileResourceLoader(ZipFile file) {
        this.file = file;

        if (this.exists(MANIFEST_PATH)) {
            try (InputStream stream = this.getStream(MANIFEST_PATH)) {
                String name = new Manifest(stream).getMainAttributes().getValue(MANIFEST_ATTRIBUTE);

                this.manifest = name != null ? new PluginManifest(name) : null;
            } catch (IOException e) {
                throw new RuntimeException("Cannot obtain manifest zip entry", e);
            }
        } else {
            this.manifest = null;
        }
    }

    public ZipFileResourceLoader(Path path) throws IOException {
        this(new ZipFile(path.toFile()));
    }

    @Override
    public PluginManifest getPluginManifest() {
        return this.manifest;
    }

    @Override
    public InputStream getStream(String name) throws IOException {
        return this.file.getInputStream(this.getZipEntry(name));
    }

    @Override
    public boolean exists(String name) {
        return this.file.getEntry(name) != null;
    }

    @Override
    public String getSource() {
        return this.file.toString();
    }

    private ZipEntry getZipEntry(String name) throws IOException {
        ZipEntry entry = this.file.getEntry(name);

        if (entry == null) {
            throw new IOException(String.format("Entry does not exist: %s", name));
        }

        return entry;
    }
}
