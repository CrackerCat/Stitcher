package me.jellysquid.stitcher.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class StreamHelper {
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Reads all bytes from the provided {@link InputStream} and returns a {@link byte[]} containing them.
     *
     * @param in             The {@link InputStream} to read from
     * @return A byte array containing all bytes read from {@param in}
     * @throws IOException If an I/O exception occurs while reading from {@param in}
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];

        int len;

        while ((len = in.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }

        return bout.toByteArray();
    }
}
