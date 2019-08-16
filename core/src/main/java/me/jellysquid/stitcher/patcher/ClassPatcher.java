package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.StitcherEnvironment;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public final class ClassPatcher {
    private final String source;
    private final Type target;

    private final List<ClassTransformer> transformers;

    ClassPatcher(String source, Type target, List<ClassTransformer> transformers) {
        this.source = source;
        this.target = target;

        this.transformers = this.sortAndVerifyTransformers(new ArrayList<>(transformers));
    }

    private List<ClassTransformer> sortAndVerifyTransformers(List<ClassTransformer> transformers) {
        // Descending order
        transformers.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));

        if (StitcherEnvironment.isTracingEnabled()) {
            Stitcher.LOGGER.trace("Transformation order for " + this.target);

            for (ClassTransformer transformer : transformers) {
                Stitcher.LOGGER.trace(" - [{}] {}", String.format("%05d", transformer.getPriority()), transformer.toString());
            }
        }

        return transformers;
    }

    public final boolean transformClass(ClassNode classNode) throws TransformerException {
        long start = System.nanoTime();

        int transformations = 0;

        for (ClassTransformer transformer : this.transformers) {
            try {
                if (transformer.transform(classNode)) {
                    transformations += 1;
                }
            } catch (Exception e) {
                throw new TransformerException("Failed to apply transformation " + transformer, e);
            }
        }

        long end = System.nanoTime();

        Stitcher.LOGGER.debug("Applied {} class transformations to class {} in {}ms", transformations, classNode.name, (end - start) / 1_000_000);

        return transformations > 0;
    }

    public Type getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        return String.format("ClassPatcher{target='%s',src='%s'}", this.target, this.source);
    }
}
