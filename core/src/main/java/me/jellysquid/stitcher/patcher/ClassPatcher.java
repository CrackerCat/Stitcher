package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.StitcherEnvironment;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public final class ClassPatcher {
    private final Type target;

    private final List<ClassTransformer> transformers = new ArrayList<>();

    private boolean isSorted = false;

    public ClassPatcher(Type target) {
        this.target = target;
    }

    private void sortTransformers() {
        // Descending order
        this.transformers.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));

        if (StitcherEnvironment.isTracingEnabled()) {
            Stitcher.LOGGER.trace(String.format("Sorting transformers for target: %s", this.target));

            for (ClassTransformer transformer : this.transformers) {
                Stitcher.LOGGER.trace(" - [{}] {}", String.format("%05d", transformer.getPriority()), transformer.toString());
            }
        }

        this.isSorted = true;
    }

    public final boolean transformClass(ClassNode classNode) throws TransformerException {
        long start = System.nanoTime();

        if (!this.isSorted) {
            this.sortTransformers();
        }

        int transformations = 0;

        for (ClassTransformer transformer : this.transformers) {
            try {
                if (transformer.transform(classNode)) {
                    transformations += 1;
                }
            } catch (Exception e) {
                throw new TransformerException(String.format("Failed to apply transformation %s", transformer), e);
            }
        }

        long end = System.nanoTime();

        Stitcher.LOGGER.debug("Applied {} class transformations to class {} in {}ms", transformations, classNode.name, (end - start) / 1_000_000);

        return transformations > 0;
    }

    public Type getTarget() {
        return this.target;
    }

    public void addTransformers(List<ClassTransformer> transformers) {
        this.transformers.addAll(transformers);

        this.isSorted = false;
    }

    @Override
    public String toString() {
        return String.format("ClassPatcher{target='%s'}", this.target);
    }
}
