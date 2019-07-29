package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class AtVariable extends InstructionMatcher {
    private final Operation operation;

    private final int index;

    public AtVariable(Operation operation, AnnotationParser where) {
        this.operation = operation;
        this.index = where.getValue("varIndex", Integer.class);
    }

    @Override
    protected boolean matches(MethodNode method, AbstractInsnNode node) {
        if (node.getType() == AbstractInsnNode.VAR_INSN) {
            if (((VarInsnNode) node).var != this.index) {
                return false;
            }

            if (this.operation == Operation.LOAD) {
                return node.getOpcode() >= Opcodes.ILOAD && node.getOpcode() <= Opcodes.SALOAD;
            } else if (this.operation == Operation.STORE) {
                return node.getOpcode() >= Opcodes.ISTORE && node.getOpcode() <= Opcodes.SASTORE;
            }
        }

        return false;
    }

    public enum Operation {
        LOAD,
        STORE
    }
}
