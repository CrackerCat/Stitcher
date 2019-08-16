package me.jellysquid.stitcher.patcher;

import org.objectweb.asm.Type;

import java.util.List;

public class TransformationData {
    private final Type target;
    private final List<ClassTransformer> transformers;

    public TransformationData(Type target, List<ClassTransformer> transformers) {
        this.target = target;
        this.transformers = transformers;
    }

    public List<ClassTransformer> getTransformers() {
        return this.transformers;
    }

    public Type getTarget() {
        return this.target;
    }
}
