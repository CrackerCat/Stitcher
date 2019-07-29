package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.inject.needle.Needle;
import me.jellysquid.stitcher.inject.needle.NeedleFactory;
import me.jellysquid.stitcher.inject.slice.SliceRange;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;

public class AtHead implements NeedleFactory {
    private static final String INITIALIZER_NAME = "<init>";

    private Needle find(MethodNode method, SliceRange slice) {
        if (method.name.equals(INITIALIZER_NAME)) {
            for (AbstractInsnNode node : slice) {
                if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) node;

                    if (methodInsnNode.name.equals(AtHead.INITIALIZER_NAME)) {
                        return new Needle(method, node);
                    }
                }
            }
        } else {
            for (AbstractInsnNode node : slice) {
                if (node.getType() == AbstractInsnNode.LABEL) {
                    return new Needle(method, node);
                }
            }

            return new Needle(method, null);
        }

        return null;
    }

    @Override
    public List<Needle> findAll(MethodNode method, SliceRange slice) {
        Needle site = this.find(method, slice);

        if (site != null) {
            return Collections.singletonList(site);
        }

        return Collections.emptyList();
    }
}
