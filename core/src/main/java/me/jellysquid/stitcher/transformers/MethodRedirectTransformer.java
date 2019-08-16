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
import java.util.List;

public class MethodRedirectTransformer implements ClassTransformer {
	private final Collection<MethodRef> targets;

	private final MethodRef site;

    private final MethodNode method;

    private final Type[] argumentTypes;

    private final Type returnType;

    private final LocalVariableCapture capture;

	public MethodRedirectTransformer(Collection<MethodRef> targets, MethodRef site, MethodNode method, LocalVariableCapture capture) {
        this.targets = targets;
        this.site = site;
        this.method = method;
        this.capture = capture;

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
                    if ((this.method.access & Opcodes.ACC_STATIC) == 0) {
                        throw new TransformerException("Method redirect must be static");
                    }

                    Validate.areMethodReturnTypesEqual(Type.getReturnType(methodInsnNode.desc), this.returnType);

                    methodInsnNode.name = this.method.name;
                    methodInsnNode.desc = this.method.desc;

                    methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);

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
        return String.format("MethodRedirectTransformer{targets=%s, site=%s}", this.targets, this.site);
    }

    public static class Builder implements ClassTransformerFactory {
        @Override
        public ClassTransformer build(PluginGroupConfig config, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            AnnotationParser values = new AnnotationParser(annotation);

			List<MethodRef> targets = new ArrayList<>();

            for (AnnotationNode targetAnnotation : values.getList("targets", AnnotationNode.class)) {
				targets.add(new MethodRef(new AnnotationParser(targetAnnotation)));
            }

			MethodRef site = new MethodRef(values.parseAnnotation("site"));

            LocalVariableCapture captures = LocalVariableCapture.buildCaptures(method);

            return new MethodRedirectTransformer(targets, site, method, captures);
        }
    }
}
