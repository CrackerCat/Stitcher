package me.jellysquid.stitcher.matchers.at;

import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.inject.SliceRange;
import me.jellysquid.stitcher.matchers.InstructionMatcher;
import me.jellysquid.stitcher.remap.MethodRef;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class AtInvoke implements InstructionMatcher {
    private final MethodRef site;

    public AtInvoke(AnnotationParser annotation) {
        this.site = new MethodRef(annotation.parseAnnotation("method"));
    }

    @Override
    public List<Needle> findAll(MethodNode method, SliceRange slice) {
        List<Needle> needles = new ArrayList<>();

        for (AbstractInsnNode node : slice) {
            if (node.getType() == AbstractInsnNode.METHOD_INSN && node.getOpcode() >= Opcodes.INVOKEVIRTUAL && node.getOpcode() <= Opcodes.INVOKEDYNAMIC) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) node;

                if (this.site.matches(methodInsnNode)) {
                    AbstractInsnNode next = node.getNext();

                    if (next != null && next.getType() == AbstractInsnNode.VAR_INSN) {
                        if (next.getOpcode() >= Opcodes.ISTORE && next.getOpcode() <= Opcodes.SASTORE) {
                            needles.add(new Needle(method, next));

                            continue;
                        }
                    }

                    needles.add(new Needle(method, node));
                }
            }
        }

        return needles;
    }
}
