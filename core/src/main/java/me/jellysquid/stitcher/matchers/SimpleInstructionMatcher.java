package me.jellysquid.stitcher.matchers;

import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.inject.SliceRange;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleInstructionMatcher implements InstructionMatcher {
    @Override
    public List<Needle> findAll(MethodNode method, SliceRange slice) {
        List<Needle> needles = new ArrayList<>();

        for (AbstractInsnNode node : slice) {
            if (this.matches(method, node)) {
                needles.add(new Needle(method, node));
            }
        }

        return needles;
    }

    protected abstract boolean matches(MethodNode method, AbstractInsnNode node);
}
