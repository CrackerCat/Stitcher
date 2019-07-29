package me.jellysquid.stitcher.plugin.resources;

import me.jellysquid.stitcher.plugin.PluginManifest;
import me.jellysquid.stitcher.plugin.PluginResourceProvider;
import me.jellysquid.stitcher.util.StreamHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileResourceProvider implements PluginResourceProvider {
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String MANIFEST_ATTRIBUTE = "Stitcher-Plugin-Name";

    private final ZipFile file;

    private final PluginManifest manifest;

    public ZipFileResourceProvider(ZipFile file) {
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

    public ZipFileResourceProvider(Path path) throws IOException {
        this(new ZipFile(path.toFile()));
    }

    @Override
    public PluginManifest getPluginManifest() {
        return this.manifest;
    }

    @Override
    public byte[] getBytes(String name) throws IOException {
        ZipEntry entry = this.getZipEntry(name);

        try (InputStream in = this.file.getInputStream(entry)) {
            return StreamHelper.toByteArray(in);
        }
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
            throw new IOException("Entry does not exist: " + name);
        }

        return entry;
    }
}
