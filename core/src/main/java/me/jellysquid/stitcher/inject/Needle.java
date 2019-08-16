package me.jellysquid.stitcher.inject;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents an injection site within a list of instructions. If {@link Needle#node} is null, the needle
 * is understood to inject into the beginning of the instruction list. Otherwise, the needle will inject immediately
 * after the specified node.
 */
public class Needle {
    private final InsnList instructions;

    private AbstractInsnNode node;

    private boolean erases;

    public Needle(MethodNode method, AbstractInsnNode after) {
        this.instructions = method.instructions;
        this.node = after;
    }

    public void shift(int offset) {
        int steps = Math.abs(offset);

        boolean forwards = offset > 0;

        for (int i = 0; this.node != null && i < steps; i++) {
            this.node = forwards ? this.node.getNext() : this.node.getPrevious();
        }
    }

    public AbstractInsnNode getInstruction() {
        return this.node;
    }

    public void inject(InsnList list) {
        this.instructions.insert(this.node, list);

        if (this.erases) {
            this.instructions.remove(this.node);
        }
    }

    public void setErases(boolean erases) {
        this.erases = erases;
    }
}
