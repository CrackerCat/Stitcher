package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;

public abstract class ClassTransformer {
    private static final int DEFAULT_PRIORITY = 0;

    protected int priority;

    protected ClassTransformer() {
        this(DEFAULT_PRIORITY);
    }

    protected ClassTransformer(int priority) {
        this.priority = priority;
    }

    public abstract boolean transform(ClassNode classNode) throws TransformerException;

    public final int getPriority() {
        return this.priority;
    }
}
