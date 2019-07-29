package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.inject.needle.Needle;
import me.jellysquid.stitcher.inject.needle.NeedleFactory;
import me.jellysquid.stitcher.inject.slice.SliceRange;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public abstract class InstructionMatcher implements NeedleFactory {
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
