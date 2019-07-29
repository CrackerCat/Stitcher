package me.jellysquid.stitcher.inject.needle;

import me.jellysquid.stitcher.annotations.At;
import me.jellysquid.stitcher.inject.*;
import me.jellysquid.stitcher.inject.slice.SliceRange;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public interface NeedleFactory {
    List<Needle> findAll(MethodNode method, SliceRange slice);

    static NeedleFactory create(AnnotationParser where) {
        At at = where.getEnum("at", At.class);

        switch (at) {
            case TAIL:
                return new AtTail();
            case HEAD:
                return new AtHead();
            case CONSTANT:
                return new AtConstant(where);
            case INVOKE:
                return new AtInvoke(where);
            case LOAD:
                return new AtVariable(AtVariable.Operation.LOAD, where);
            case STORE:
                return new AtVariable(AtVariable.Operation.STORE, where);
            default:
                throw new IllegalArgumentException("Cannot instantiate instruction matcher for clause At#" + at.name());
        }
    }

    default Needle findFirst(MethodNode methodNode, SliceRange slice) {
        List<Needle> needles = this.findAll(methodNode, slice);

        if (needles.isEmpty()) {
            return null;
        }

        return needles.get(0);
    }
}
