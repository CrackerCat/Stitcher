package me.jellysquid.stitcher.matchers;

import me.jellysquid.stitcher.annotations.Where;
import me.jellysquid.stitcher.inject.Needle;
import me.jellysquid.stitcher.inject.SliceRange;
import me.jellysquid.stitcher.matchers.at.*;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public interface InstructionMatcher {
    List<Needle> findAll(MethodNode method, SliceRange slice);

    static InstructionMatcher create(AnnotationParser where) {
        Where.At at = where.getEnum("at", Where.At.class);

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
                throw new IllegalArgumentException(String.format("Cannot instantiate instruction matcher for clause At#%s", at.name()));
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
