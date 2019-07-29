package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;

public interface ClassTransformer {
    boolean transform(ClassNode classNode) throws TransformerException;
}
