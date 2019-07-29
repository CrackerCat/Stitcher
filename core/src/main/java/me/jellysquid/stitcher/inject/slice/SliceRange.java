package me.jellysquid.stitcher.inject.slice;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;

public class SliceRange implements Iterable<AbstractInsnNode> {
    private final InsnList instructions;

    private final AbstractInsnNode start, end;

    public SliceRange(InsnList list, AbstractInsnNode start, AbstractInsnNode end) {
        this.instructions = list;

        this.start = start;
        this.end = end;
    }

    public static SliceRange all(InsnList list) {
        return new SliceRange(list, list.getFirst(), list.getLast());
    }

    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return new RangeIterator(this);
    }

    /**
     * Optimized iterator which works on an array rather than a doubly linked list. Massively improves
     * iteration performance when the same instruction list is being iterated over by multiple {@link SliceRange} or
     * when the flat array is already constructed by earlier code.
     */
    private static class RangeIterator implements Iterator<AbstractInsnNode> {
        private final AbstractInsnNode[] cache;
        private final int end;

        private int index = 0;

        private RangeIterator(SliceRange range) {
            this.cache = range.instructions.toArray();

            if (range.start != null) {
                this.index = range.instructions.indexOf(range.start);
            }

            if (range.end != null) {
                this.end = range.instructions.indexOf(range.end);
            } else {
                this.end = range.instructions.size() - 1;
            }
        }

        @Override
        public boolean hasNext() {
            return this.index <= this.end;
        }

        @Override
        public AbstractInsnNode next() {
            return this.cache[this.index++];
        }
    }
}
