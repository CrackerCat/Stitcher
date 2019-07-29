package me.jellysquid.stitcher.util.exceptions;

/**
 * Indicates an exception that occurred during the transformation of a class.
 */
public class TransformerException extends Exception {
    public TransformerException(String message) {
        super(message);
    }

    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}
