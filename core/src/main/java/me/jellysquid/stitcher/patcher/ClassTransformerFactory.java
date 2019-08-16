package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

public interface ClassTransformerFactory {
    ClassTransformer build(MethodNode method, AnnotationNode annotation) throws TransformerBuildException;
}
