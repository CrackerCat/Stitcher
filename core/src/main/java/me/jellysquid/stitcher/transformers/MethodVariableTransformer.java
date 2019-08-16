package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.matchers.at.AtVariable;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodVariableTransformer extends MethodInjectionTransformer {
    private MethodVariableTransformer(MethodNode method, AnnotationNode node) throws TransformerBuildException {
        super(method, node);

        if (!(this.matcher.getNeedleFactory() instanceof AtVariable)) {
            throw new IllegalArgumentException("Matcher must be either LOAD or STORE");
        }

        if (this.offset != 0) {
            throw new IllegalArgumentException("Offset cannot be specified for @MethodVariable");
        }
    }

    @Override
    public void inject(ClassNode classNode, MethodNode method, Needle needle) throws TransformerException {
        needle.setErases(true);

        super.inject(classNode, method, needle);
    }

    public static class Builder implements ClassTransformerFactory {
        @Override
        public ClassTransformer build(PluginGroupConfig config, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            return new MethodVariableTransformer(method, annotation);
        }
    }
}
