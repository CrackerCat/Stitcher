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

    private final AbstractInsnNode node;

    public Needle(MethodNode method, AbstractInsnNode after) {
        this(method.instructions, after);
    }

    public Needle(InsnList instructions, AbstractInsnNode after) {
        this.instructions = instructions;
        this.node = after;
    }

    public AbstractInsnNode getNode() {
        return this.node;
    }

    public void inject(InsnList list) {
        this.instructions.insert(this.node, list);
    }

    public Needle shift(int shift) {
        if (shift == 0) {
            return this;
        }

        AbstractInsnNode node = this.node;

        for (int i = 0; this.node != null && i < Math.abs(shift); i++) {
            node = shift > 0 ? this.node.getNext() : this.node.getPrevious();
        }

        return new Needle(this.instructions, node);
    }
}
