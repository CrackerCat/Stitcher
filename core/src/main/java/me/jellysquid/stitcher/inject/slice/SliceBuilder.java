package me.jellysquid.stitcher.inject.slice;

import me.jellysquid.stitcher.inject.needle.NeedleFactory;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class SliceBuilder {
    private final NeedleFactory start, end;

    private SliceBuilder(NeedleFactory start, NeedleFactory end) {
        this.start = start;
        this.end = end;
    }

    public static SliceBuilder createSliceMatcher(List<AnnotationNode> list) {
        if (list == null || list.isEmpty()) {
            return new SliceBuilder(null, null);
        }

        return createSliceMatcher(new AnnotationParser(list.get(0)));
    }

    private static SliceBuilder createSliceMatcher(AnnotationParser values) {
        NeedleFactory startMatcher = NeedleFactory.create(values.parseAnnotation("start"));
        NeedleFactory endMatcher = NeedleFactory.create(values.parseAnnotation("end"));

        return new SliceBuilder(startMatcher, endMatcher);
    }

    public SliceRange buildRange(MethodNode methodNode) {
        AbstractInsnNode start = null, end = null;

        if (this.start != null) {
            start = this.start.findFirst(methodNode, SliceRange.all(methodNode.instructions)).getInstruction();
        }

        if (this.end != null) {
            SliceRange slice;

            if (start != null) {
                slice = new SliceRange(methodNode.instructions, start, null);
            } else {
                slice = SliceRange.all(methodNode.instructions);
            }

            end = this.end.findFirst(methodNode, slice).getInstruction();
        }

        return new SliceRange(methodNode.instructions, start, end);
    }
}
