package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodVariableTransformer extends MethodInjectionTransformer {
    public MethodVariableTransformer(PluginResource source, MethodNode method, AnnotationNode node) throws TransformerBuildException {
        super(source, method, node);
    }

    @Override
    public void inject(ClassNode classNode, MethodNode method, Needle needle) throws TransformerException {
        AbstractInsnNode node = needle.getNode();

        if ((node.getOpcode() < Opcodes.ILOAD || node.getOpcode() > Opcodes.SALOAD) &&
                (node.getOpcode() < Opcodes.ISTORE || node.getOpcode() > Opcodes.SASTORE)) {
            throw new TransformerException("Cannot modify instructions other than LOAD/STORE");
        }

        super.inject(classNode, method, needle);

        method.instructions.remove(needle.getNode());
    }
}
