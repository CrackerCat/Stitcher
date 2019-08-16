package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ClassFieldTransformer extends ClassTransformer {
    private final FieldNode field;

    public ClassFieldTransformer(PluginResource source, FieldNode field, int priority) {
        super(source, priority);

        this.field = field;
    }

    @Override
    public boolean transform(ClassNode classNode) throws TransformerException {
        classNode.fields.add(this.field);

        return true;
    }

    @Override
    public String toString() {
        return String.format("ClassFieldTransformer{name='%s'}", this.field.name);
    }
}
