package me.jellysquid.stitcher.util;

import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Validate {
    public static void areMethodArgumentsEqual(Type[] a, Type[] b) throws TransformerException {
        if (!Arrays.equals(a, b)) {
            throw new TransformerException(String.format("The overwriting method does not have the same method" +
                    " arguments as the target method (needed: [%s], found: [%s])", formatMethodArgumentTypes(a), formatMethodArgumentTypes(b)));
        }
    }

    private static String formatMethodArgumentTypes(Type[] types) {
        return Arrays.stream(types).map(Type::getDescriptor).collect(Collectors.joining(", "));
    }

    public static void areMethodReturnTypesEqual(Type a, Type b) throws TransformerException {
        if (!a.equals(b)) {
            throw new TransformerException(String.format("The overwriting method does not have the same return type" +
                    " as the target method (needed: '%s', found: '%s')", a.getDescriptor(), b.getDescriptor()));
        }
    }
}
