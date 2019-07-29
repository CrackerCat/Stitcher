package me.jellysquid.stitcher.util;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;

public class AnnotationParser {
    private final HashMap<String, Object> map = new HashMap<>();

    public AnnotationParser(AnnotationNode node) {
        Iterator<Object> it = node.values.iterator();

        int i = 0;

        while (it.hasNext()) {
            String key = (String) it.next();

            if (!it.hasNext()) {
                throw new RuntimeException(String.format("Annotation key/value pair at index %d is missing a value", i));
            }

            this.map.put(key, it.next());

            i++;
        }
    }

    public <T> List<T> getList(String key, Class<T> type) {
        List<?> raw = this.getValue(key, List.class, null);

        if (raw == null) {
            return Collections.emptyList();
        }

        List<T> ret = new ArrayList<>(raw.size());

        for (Object val : raw) {
            if (!type.isInstance(val)) {
                throw new ClassCastException(String.format("Entry does not match type %s (found: %s)", type, val.getClass()));
            }

            ret.add(type.cast(val));
        }

        return ret;
    }

    public AnnotationParser parseAnnotation(String key) {
        return new AnnotationParser(this.getValue(key, AnnotationNode.class));
    }

    public <T> T getValue(String key, Class<T> type) {
        T result = this.getValue(key, type, null);

        if (result == null) {
            throw new NullPointerException(String.format("Value for '%s' is not defined and no default value exists", key));
        }

        return result;
    }

    public <T> T getValue(String key, Class<T> clazz, T def) {
        Object obj = this.map.get(key);

        if (obj == null) {
            return def;
        }

        if (obj instanceof List && !clazz.isAssignableFrom(List.class)) {
            List<?> list = (List<?>) obj;

            if (list.isEmpty()) {
                throw new NullPointerException(String.format("Value for '%s' contains no elements", key));
            }

            obj = list.get(0);
        }

        if (!clazz.isInstance(obj)) {
            throw new ClassCastException(String.format("Value for '%s' is not of type %s (value's type is: %s)", key, clazz, obj.getClass()));
        }

        return clazz.cast(obj);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> type) {
        T result = this.getEnum(key, type, null);

        if (result == null) {
            throw new NullPointerException(String.format("Property '%s' is not defined and no default value exists", key));
        }

        return result;
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> type, T def) {
        Object obj = this.map.get(key);

        if (obj == null) {
            return def;
        }

        if (!(obj instanceof String[])) {
            throw new ClassCastException(String.format("Value assigned to '%s' is not a string array (value's type is: %s)", key, obj.getClass()));
        }

        String[] arr = (String[]) obj;

        if (arr.length != 2) {
            throw new ClassCastException(String.format("Value assigned to '%s' is not a valid enumeration representation", key));
        }

        String name = arr[1];

        try {
            return Enum.valueOf(type, name);
        } catch (NullPointerException e) {
            throw new ClassCastException(String.format("Value assigned to '%s' does not represent a value defined by the enum %s (enum name: '%s')", key, type, name));
        }
    }
}
