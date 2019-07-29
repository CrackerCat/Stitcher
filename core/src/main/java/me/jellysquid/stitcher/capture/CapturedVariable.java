package me.jellysquid.stitcher.capture;

import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CapturedVariable {
    private final Type argumentType;

    private final int argumentSort;

    private final int localIndex;

    CapturedVariable(Type argumentType, int localIndex) {
        this.argumentType = argumentType;
        this.argumentSort = argumentType.getSort();
        this.localIndex = localIndex;
    }

    private void validateLocalVariableTable(MethodNode methodNode) throws TransformerException {
        if (this.localIndex > methodNode.maxLocals) {
            throw new TransformerException("Cannot capture local variable at index " + this.localIndex +
                    " because it exceeds the size of the local variable table (table size: " + methodNode.maxLocals + ")");
        }

        LocalVariableNode node = null;

        for (LocalVariableNode next : methodNode.localVariables) {
            if (next.index == this.localIndex) {
                node = next;

                break;
            }
        }

        if (node == null) {
            throw new TransformerException("The local variable at index " + this.localIndex + " does not exist");
        }

        if (!this.argumentType.getDescriptor().equals(node.desc)) {
            throw new TransformerException("Cannot capture local variable at index " + this.localIndex +
                    " because the destination parameter is of the incorrect type (expected: " + this.argumentType + "," +
                    " found: " + node.desc + ")");
        }
    }

    public VarInsnNode createLoadInstruction(MethodNode methodNode) throws TransformerException {
        this.validateLocalVariableTable(methodNode);

        return new VarInsnNode(this.getLoadOpcode(), this.localIndex);
    }

    private int getLoadOpcode() {
        switch (this.argumentSort) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.CHAR:
                return Opcodes.ILOAD;
            case Type.LONG:
                return Opcodes.LLOAD;
            case Type.FLOAT:
                return Opcodes.FLOAD;
            case Type.DOUBLE:
                return Opcodes.DLOAD;
            default:
                return Opcodes.ALOAD;
        }
    }
}
