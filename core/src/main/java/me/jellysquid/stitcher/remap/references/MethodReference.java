package me.jellysquid.stitcher.remap.references;

import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodReference {
    private final String name;

    private final String desc;

    public MethodReference(AnnotationParser ann) {
        this.name = ann.getValue("value", String.class);
        this.desc = ann.getValue("desc", String.class);
    }

    @Override
    public String toString() {
        return String.format("MethodReference{name='%s', desc='%s'}", this.name, this.desc);
    }

    public boolean matches(MethodInsnNode node) {
        return node.name.equals(this.name) && node.desc.equals(this.desc);
    }

    public boolean matches(MethodNode method) {
        return method.name.equals(this.name) && method.desc.equals(this.desc);
    }
}
