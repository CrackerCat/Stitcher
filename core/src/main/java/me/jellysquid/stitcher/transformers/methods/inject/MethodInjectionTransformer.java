package me.jellysquid.stitcher.transformers.methods.inject;

import me.jellysquid.stitcher.annotations.Inject;
import me.jellysquid.stitcher.capture.LocalVariableCapture;
import me.jellysquid.stitcher.inject.needle.NeedleMatcher;
import me.jellysquid.stitcher.inject.needle.Needle;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.patcher.ClassTransformerFactory;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.remap.references.MethodReference;
import me.jellysquid.stitcher.util.ASMHelper;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

public class MethodInjectionTransformer implements ClassTransformer {
    protected final MethodReference target;

    protected final LocalVariableCapture localCapture;

    protected final MethodNode method;

    protected final NeedleMatcher matcher;

    protected final int offset;

    protected MethodInjectionTransformer(MethodReference target, LocalVariableCapture localCapture, MethodNode method, NeedleMatcher matcher, int offset) {
        this.target = target;
        this.localCapture = localCapture;
        this.method = method;
        this.matcher = matcher;
        this.offset = offset;

        this.method.name = getUniqueMethodName(this.method);
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        MethodNode methodNode = ASMHelper.findMethod(classNode, this.target);

        List<Needle> sites = this.matcher.findAll(methodNode);

        if (!sites.isEmpty()) {
            for (Needle needle : sites) {
                needle.shift(this.offset);

                this.inject(classNode, methodNode, needle);
            }

            classNode.methods.add(this.method);

            return true;
        }

        return false;
    }

    public void inject(ClassNode classNode, MethodNode method, Needle needle) throws TransformerException {
        InsnList list = new InsnList();

        boolean isStatic = (this.method.access & Opcodes.ACC_STATIC) != 0;

        if (!isStatic) {
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        list.add(this.localCapture.createLoadInstructions(method));
        list.add(new MethodInsnNode(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL,
                classNode.name, this.method.name, this.method.desc, false));

        needle.shift(this.offset);
        needle.inject(list);
    }

    private static String getUniqueMethodName(MethodNode method) {
        int hash = method.name.hashCode();
        hash = 37 * hash + method.desc.hashCode();

        return method.name + "$" + Integer.toString(Math.abs(hash), 36);
    }

    @Override
    public String toString() {
        return String.format("MethodInjectionTransformer{impl='%s', target='%s'}", this.method.name, this.target);
    }

    public static class Builder implements ClassTransformerFactory {
        @Override
        public ClassTransformer build(PluginGroupConfig config, MethodNode method, AnnotationNode annotation) throws TransformerBuildException {
            AnnotationParser inject = new AnnotationParser(annotation);

            MethodReference ref = new MethodReference(inject.parseAnnotation("target"));
            AnnotationParser where = inject.parseAnnotation("where");

            NeedleMatcher matcher = NeedleMatcher.build(inject, where);
            LocalVariableCapture captures = LocalVariableCapture.buildCaptures(method);

            int offset = where.getValue("offset", Integer.class, 0);

            return this.create(ref, captures, method, matcher, offset);
        }

        protected ClassTransformer create(MethodReference target, LocalVariableCapture localCapture, MethodNode method, NeedleMatcher matcher, int offset) {
            return new MethodInjectionTransformer(target, localCapture, method, matcher, offset);
        }
    }
}
