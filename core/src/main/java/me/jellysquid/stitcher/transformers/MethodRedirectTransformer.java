package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.capture.LocalVariableCapture;
import me.jellysquid.stitcher.inject.SliceRange;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.remap.MethodRef;
import me.jellysquid.stitcher.util.ASMHelper;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.Validate;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;

public class MethodRedirectTransformer extends ClassTransformer {
    private final Collection<MethodRef> targets;

    private final MethodRef site;

    private final MethodNode method;

    private final Type[] argumentTypes;

    private final Type returnType;

    private final LocalVariableCapture capture;

    public MethodRedirectTransformer(MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
        this.method = method;

        AnnotationParser values = new AnnotationParser(annotation);

        this.targets = new ArrayList<>();

        for (AnnotationNode targetAnnotation : values.getList("targets", AnnotationNode.class)) {
            this.targets.add(new MethodRef(new AnnotationParser(targetAnnotation)));
        }

        this.site = new MethodRef(values.parseAnnotation("site"));

        this.capture = LocalVariableCapture.buildCaptures(method);
        this.priority = values.getValue("priority", Integer.class, 0);

        this.argumentTypes = Type.getArgumentTypes(this.method.desc);
        this.returnType = Type.getReturnType(this.method.desc);
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        boolean modified = false;

        for (MethodRef target : this.targets) {
            modified |= this.apply(ASMHelper.findMethod(classNode, target));
        }

        if (modified) {
            classNode.methods.add(this.method);
        }

        return modified;
    }

    private boolean apply(MethodNode methodNode) throws TransformerException {
        boolean modified = false;

        for (AbstractInsnNode insnNode : SliceRange.all(methodNode.instructions)) {
            if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

                if (this.site.matches(methodInsnNode)) {
                    boolean staticRedirect = (methodNode.access & Opcodes.ACC_STATIC) != 0;
                    boolean staticSite = (this.method.access & Opcodes.ACC_STATIC) != 0;

                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        if (!staticRedirect) {
                            throw new TransformerException("Method redirect must be static as call site is from within a static method");
                        }
                    } else if (staticRedirect) {
                        throw new TransformerException("Method redirect must be non-static as call site is from within a non-static method");
                    }

                    Validate.areMethodReturnTypesEqual(Type.getReturnType(methodInsnNode.desc), this.returnType);

                    methodInsnNode.name = this.method.name;
                    methodInsnNode.desc = this.method.desc;

                    methodNode.instructions.insertBefore(methodInsnNode, this.capture.createLoadInstructions(methodNode));

                    modified = true;
                }
            }
        }

        if (!modified) {
            throw new TransformerException("Failed to locate site to apply patch");
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("MethodRedirectTransformer{targets=%s, site=%s, destination='%s'}", this.targets, this.site, this.method.name);
    }

    public static class Builder implements ClassTransformerFactory {
        @Override
        public ClassTransformer build(PluginGroupConfig config, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            return new MethodRedirectTransformer(method, annotation);
        }
    }
}
