package me.jellysquid.stitcher.transformers.methods.inject;

import me.jellysquid.stitcher.capture.LocalVariableCapture;
import me.jellysquid.stitcher.inject.AtVariable;
import me.jellysquid.stitcher.inject.needle.Needle;
import me.jellysquid.stitcher.inject.needle.NeedleMatcher;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.remap.references.MethodReference;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodVariableTransformer extends MethodInjectionTransformer {
    private MethodVariableTransformer(MethodReference target, LocalVariableCapture localCapture, MethodNode method, NeedleMatcher needleMatcher, int offset) {
        super(target, localCapture, method, needleMatcher, offset);
    }

    @Override
    public void inject(ClassNode classNode, MethodNode method, Needle needle) throws TransformerException {
        needle.setErases(true);

        super.inject(classNode, method, needle);
    }

    public static class Builder extends MethodInjectionTransformer.Builder {
        @Override
        protected ClassTransformer create(MethodReference target, LocalVariableCapture localCapture, MethodNode method, NeedleMatcher matcher, int offset) {
            if (!(matcher.getNeedleFactory() instanceof AtVariable)) {
                throw new IllegalArgumentException("Matcher must be either LOAD or STORE");
            }

            if (offset != 0) {
                throw new IllegalArgumentException("Offset cannot be specified for @MethodVariable");
            }

            return new MethodVariableTransformer(target, localCapture, method, matcher, offset);
        }
    }
}
