package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.capture.LocalVariableCapture;
import me.jellysquid.stitcher.inject.SliceRange;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.PluginResource;
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

    public MethodRedirectTransformer(PluginResource source, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
        super(source);

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
            modified |= this.apply(classNode, ASMHelper.findMethod(classNode, target));
        }

        if (modified) {
            classNode.methods.add(this.method);
        }

        return modified;
    }

    private boolean apply(ClassNode classNode, MethodNode methodNode) throws TransformerException {
        boolean modified = false;

        for (AbstractInsnNode insnNode : SliceRange.all(methodNode.instructions)) {
            if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

                if (this.site.matches(methodInsnNode)) {
                    boolean isStaticInvoke = methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC;
                    boolean isStaticRedirect = (this.method.access & Opcodes.ACC_STATIC) != 0;

                    if (isStaticInvoke && !isStaticRedirect) {
                        throw new TransformerException("The redirect method must be static when redirecting a static method call");
                    }

                    Validate.areMethodReturnTypesEqual(Type.getReturnType(methodInsnNode.desc), this.returnType);

                    if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && isStaticRedirect) {
                        if (this.argumentTypes.length <= 0 || !classNode.name.equals(this.argumentTypes[0].getInternalName())) {
                            throw new TransformerException("The first argument of a static redirect made from a non-static call must match the type of `this`");
                        }

                        methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                    }

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
        public ClassTransformer build(PluginResource source, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            return new MethodRedirectTransformer(source, method, annotation);
        }
    }
}
