package me.jellysquid.stitcher.matchers.at;

import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.inject.SliceRange;
import me.jellysquid.stitcher.matchers.InstructionMatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class AtTail implements InstructionMatcher {
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
