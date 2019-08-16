package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.matchers.InstructionMatcher;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class NeedleMatcher {
    private final InstructionMatcher matcher;

    private final List<Integer> only;

    private final SliceBuilder sliceBuilder;

    private final int expect;

    public NeedleMatcher(InstructionMatcher matcher, List<Integer> only, SliceBuilder sliceBuilder, int expect) {
        this.matcher = matcher;
        this.only = only;
        this.sliceBuilder = sliceBuilder;
        this.expect = expect;
    }

    public static NeedleMatcher build(AnnotationParser inject, AnnotationParser where) {
        InstructionMatcher matcher = InstructionMatcher.create(where);
        List<Integer> only = inject.getList("only", Integer.class);
        SliceBuilder sliceBuilder = SliceBuilder.createSliceMatcher(inject.getList("slice", AnnotationNode.class));
        int expect = inject.getValue("expect", Integer.class, 1);

        return new NeedleMatcher(matcher, only, sliceBuilder, expect);
    }

    public List<Needle> findAll(MethodNode methodNode) throws TransformerException {
        SliceRange range = this.sliceBuilder.buildRange(methodNode);

        List<Needle> needles = this.matcher.findAll(methodNode, range);

        if (this.expect > 0 && needles.size() < this.expect) {
            throw new TransformerException("Expected " + this.expect + " match(es), but found " + needles.size() + " matches for clause");
        }

        List<Needle> filtered;

        if (this.only.isEmpty()) {
            filtered = needles;
        } else {
            filtered = new ArrayList<>(this.only.size());

            for (int ordinal : this.only) {
                if (ordinal >= needles.size() || ordinal < 0) {
                    throw new TransformerException("Selected needle index " + ordinal + " is not contained in [0," + needles.size() + ")");
                }

                filtered.add(needles.get(ordinal));
            }
        }

        return filtered;
    }

    public InstructionMatcher getNeedleFactory() {
        return this.matcher;
    }
}
