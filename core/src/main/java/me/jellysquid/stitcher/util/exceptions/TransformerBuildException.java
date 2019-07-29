package me.jellysquid.stitcher.util.exceptions;

/**
 * Indicates an exception that occurred during the building of a transformation for a class.
 */
public class TransformerBuildException extends Exception {
    public TransformerBuildException(String message) {
        super(message);
    }

    public TransformerBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
