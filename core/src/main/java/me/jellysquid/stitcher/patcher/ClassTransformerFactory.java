package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

public interface ClassTransformerFactory {
    ClassTransformer build(PluginResource source, MethodNode method, AnnotationNode annotation) throws TransformerBuildException;
}
