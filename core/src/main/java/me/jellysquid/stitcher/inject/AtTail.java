package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.inject.needle.Needle;
import me.jellysquid.stitcher.inject.needle.NeedleFactory;
import me.jellysquid.stitcher.inject.slice.SliceRange;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class AtTail implements NeedleFactory {
    @Override
    public List<Needle> findAll(MethodNode method, SliceRange slice) {
        ArrayList<Needle> sites = new ArrayList<>();

        for (AbstractInsnNode node : slice) {
            if (node.getOpcode() >= Opcodes.IRETURN && node.getOpcode() <= Opcodes.RETURN) {
                sites.add(new Needle(method, node.getPrevious()));
            }
        }

        return sites;
    }
}
