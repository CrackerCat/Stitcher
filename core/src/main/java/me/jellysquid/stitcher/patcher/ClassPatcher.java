package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class ClassPatcher {
    private final String source;
    private final Type target;

    private final List<ClassTransformer> classTransformers = new ArrayList<>();

    private final List<FieldNode> addedFields = new ArrayList<>();
    private final List<MethodNode> addedMethods = new ArrayList<>();
    private final List<String> addedInterfaces = new ArrayList<>();

    public ClassPatcher(String source, Type target) {
        this.source = source;
        this.target = target;
    }

    public final boolean transformClass(ClassNode classNode) throws TransformerException {
        long start = System.nanoTime();

        int transformations = 0;

        for (ClassTransformer transformer : this.classTransformers) {
            try {
                if (transformer.transform(classNode)) {
                    transformations += 1;
                }
            } catch (Exception e) {
                throw new TransformerException("Failed to apply transformation " + transformer, e);
            }
        }

        classNode.fields.addAll(this.addedFields);
        classNode.methods.addAll(this.addedMethods);
        classNode.interfaces.addAll(this.addedInterfaces);

        long end = System.nanoTime();

        Stitcher.LOGGER.debug("Applied {} class transformations to class {} in {}ms", transformations, classNode.name, (end - start) / 1_000_000);

        return transformations > 0;
    }

    public Type getTarget() {
        return this.target;
    }

    public void addField(FieldNode fieldNode) {
        this.addedFields.add(fieldNode);
    }

    public void addMethod(MethodNode methodNode) {
        this.addedMethods.add(methodNode);
    }

    @Override
    public String toString() {
        return String.format("ClassPatcher{target='%s',src='%s'}", this.target, this.source);
    }

    public void addInterface(String interfaceName) {
        this.addedInterfaces.add(interfaceName);
    }

    public void addTransformer(ClassTransformer transformer) {
        this.classTransformers.add(transformer);
    }
}
