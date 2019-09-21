package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.remap.MethodRef;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.Methods;
import me.jellysquid.stitcher.util.Validate;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class MethodOverwriteTransformer extends ClassTransformer {
    private final Type returnType;

    private final Type[] argumentTypes;

    private final MethodRef target;

    private final InsnList instructions;

    public MethodOverwriteTransformer(PluginResource source, MethodNode method, AnnotationNode annotation) {
        super(source);

        this.returnType = Type.getReturnType(method.desc);
        this.argumentTypes = Type.getArgumentTypes(method.desc);
        this.instructions = method.instructions;

        AnnotationParser values = new AnnotationParser(annotation);

        this.target = new MethodRef(values.parseAnnotation("target"));

        this.priority = values.getValue("priority", Integer.class, 0);
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        MethodNode methodNode = Methods.findMethod(classNode, this.target);

        Validate.areMethodArgumentsEqual(Type.getArgumentTypes(methodNode.desc), this.argumentTypes);
        Validate.areMethodReturnTypesEqual(Type.getReturnType(methodNode.desc), this.returnType);

        methodNode.instructions = this.instructions;

        return true;
    }

    @Override
    public String toString() {
        return String.format("MethodOverwriteTransformer{target=%s}", this.target);
    }
}
