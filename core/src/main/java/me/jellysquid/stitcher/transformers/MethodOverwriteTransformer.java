package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.remap.MethodRef;
import me.jellysquid.stitcher.util.ASMHelper;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.Validate;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class MethodOverwriteTransformer implements ClassTransformer {
    private final Type returnType;

    private final Type[] argumentTypes;

	private final MethodRef target;

    private final InsnList instructions;

	private MethodOverwriteTransformer(MethodRef target, Type returnType, Type[] argumentTypes, InsnList instructions) {
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
        this.target = target;
        this.instructions = instructions;
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        MethodNode methodNode = ASMHelper.findMethod(classNode, this.target);

        Validate.areMethodArgumentsEqual(Type.getArgumentTypes(methodNode.desc), this.argumentTypes);
        Validate.areMethodReturnTypesEqual(Type.getReturnType(methodNode.desc), this.returnType);

        methodNode.instructions = this.instructions;

        return true;
    }

    @Override
    public String toString() {
        return String.format("MethodOverwriteTransformer{target=%s}", this.target);
    }

    public static class Builder implements ClassTransformerFactory {
        @Override
        public ClassTransformer build(PluginGroupConfig config, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            AnnotationParser values = new AnnotationParser(annotation);

			MethodRef ref = new MethodRef(values.parseAnnotation("target"));

            return new MethodOverwriteTransformer(ref, Type.getReturnType(method.desc), Type.getArgumentTypes(method.desc), method.instructions);
        }
    }
}
