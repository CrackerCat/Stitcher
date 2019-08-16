package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;

public abstract class ClassTransformer {
    private final PluginResource source;

    private static final int DEFAULT_PRIORITY = 0;

    protected int priority;

    protected ClassTransformer(PluginResource source) {
        this(source, DEFAULT_PRIORITY);
    }

    protected ClassTransformer(PluginResource source, int priority) {
        this.source = source;
        this.priority = priority;
    }

    public abstract boolean transform(ClassNode classNode) throws TransformerException;

    public final int getPriority() {
        return this.priority;
    }
}
