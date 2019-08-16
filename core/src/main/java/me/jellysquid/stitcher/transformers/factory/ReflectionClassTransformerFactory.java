package me.jellysquid.stitcher.transformers.factory;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;

public class ReflectionClassTransformerFactory implements ClassTransformerFactory {
    private final Class<? extends ClassTransformer> type;

    private final Constructor<? extends ClassTransformer> constructor;

    public ReflectionClassTransformerFactory(Class<? extends ClassTransformer> type) {
        this.type = type;

        try {
            this.constructor = type.getConstructor(PluginResource.class, MethodNode.class, AnnotationNode.class);

            if (this.constructor.isAccessible()) {
                throw new ReflectiveOperationException("Constructor is not accessible");
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("No suitable constructor found for " + type.getName(), e);
        }
    }

    @Override
    public ClassTransformer build(PluginResource source, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
        try {
            return this.constructor.newInstance(source, method, annotation);
        } catch (ReflectiveOperationException e) {
            throw new TransformerBuildException("Failed to instantiate class transformer for type " + this.type, e);
        }
    }
}
