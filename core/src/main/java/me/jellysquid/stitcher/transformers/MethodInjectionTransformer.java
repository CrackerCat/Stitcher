package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.capture.LocalVariableCapture;
import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.inject.NeedleMatcher;
import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.remap.MethodRef;
import me.jellysquid.stitcher.util.ASMHelper;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;

public class MethodInjectionTransformer extends ClassTransformer {
    protected final MethodRef target;

    private final LocalVariableCapture localCapture;

    protected final MethodNode method;

    protected final NeedleMatcher matcher;

    protected final int offset;

    public MethodInjectionTransformer(PluginResource source, MethodNode method, AnnotationNode node) throws TransformerBuildException {
        super(source);

        this.method = method;
        this.method.name = getUniqueMethodName(this.method);

        AnnotationParser inject = new AnnotationParser(node);

        this.target = new MethodRef(inject.parseAnnotation("target"));

        AnnotationParser where = inject.parseAnnotation("where");

        this.matcher = NeedleMatcher.build(inject, where);
        this.localCapture = LocalVariableCapture.buildCaptures(method);

        this.offset = where.getValue("offset", Integer.class, 0);
        this.priority = where.getValue("priority", Integer.class, 0);
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        MethodNode methodNode = ASMHelper.findMethod(classNode, this.target);

        List<Needle> sites = this.matcher.findAll(methodNode);

        if (!sites.isEmpty()) {
            for (Needle needle : sites) {
                this.inject(classNode, methodNode, needle.shift(this.offset));
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

        needle.shift(this.offset).inject(list);
    }

    private static String getUniqueMethodName(MethodNode method) {
        int hash = method.name.hashCode();
        hash = 37 * hash + method.desc.hashCode();
        hash = 37 * hash + method.access;

        return method.name + "$" + Integer.toString(Math.abs(hash), 36);
    }

    @Override
    public String toString() {
        return String.format("MethodInjectionTransformer{impl='%s', target='%s'}", this.method.name, this.target);
    }
}
