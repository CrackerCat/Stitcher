package me.jellysquid.stitcher.util;

import me.jellysquid.stitcher.remap.references.MethodReference;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class ASMHelper {
    /**
     * Locates a method within a {@link ClassNode} by a {@link MethodReference}.
     *
     * @param classNode The {@link ClassNode} to search within
     * @param ref       The {@link MethodReference} representing the method to search for
     * @return The first {@link MethodNode} matching the description of {@param ref}.
     * @throws TransformerException If no method matching {@param ref} can be found.
     */
    public static MethodNode findMethod(ClassNode classNode, MethodReference ref) throws TransformerException {
        for (MethodNode methodNode : classNode.methods) {
            if (ref.matches(methodNode)) {
                return methodNode;
            }
        }

        throw new TransformerException("Couldn't find method matching reference " + ref.toString());
    }
}
