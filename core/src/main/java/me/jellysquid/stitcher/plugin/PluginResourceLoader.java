package me.jellysquid.stitcher.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface PluginResourceLoader {
    int BUFFER_SIZE = 1024 * 4;

    /**
     * Returns the manifest of this plugin.
     *
     * @return The manifest of the plugin, if this instance represents a valid plugin, otherwise null
     */
    PluginManifest getPluginManifest();

    /**
     * Returns a byte array containing the bytes of the specified resource contained within the plugin. This should be
     * used instead of manually creating a byte array from {@link PluginResourceLoader#getStream(String)} as to allow the
     * implementation to perform optimizations.
     *
     * @param name The name of the resource to retrieve
     * @return A byte array containing all the bytes of the resource
     * @throws IOException If an I/O exception occurs during reading or if the resource could not be found
     */
    default byte[] getBytes(String name) throws IOException {
        InputStream in = this.getStream(name);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];

        int len;

        while ((len = in.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }

        return bout.toByteArray();
    }

    /**
     * Returns a {@link InputStream} which will read bytes from the specified resource contained within the plugin.
     *
     * @param name The name of the resource to retrieve
     * @return A {@link InputStream} to the bytes of the resource
     * @throws IOException If an I/O exception occurs opening the stream or if the resource could not be found
     */
    InputStream getStream(String name) throws IOException;

    /**
     * @param name The name of the resource
     * @return True if the resource exists, otherwise false
     */
    boolean exists(String name);

    /**
     * Returns a string representing the human-friendly location this resource provider is loading from.
     */
    String getSource();
}
