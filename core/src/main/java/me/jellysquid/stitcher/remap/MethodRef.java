package me.jellysquid.stitcher.remap;

import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodRef {
    private final String owner;

    private final String name;

    private final String desc;

    public MethodRef(AnnotationParser ann) {
        this.owner = ann.getValue("owner", Type.class).getInternalName();
        this.name = ann.getValue("value", String.class);
        this.desc = ann.getValue("desc", String.class);
    }

    @Override
    public String toString() {
        return String.format("%s.%s%s", this.owner, this.name, this.desc);
    }

    public boolean matches(MethodInsnNode node) {
        return node.name.equals(this.name) && node.desc.equals(this.desc) && node.owner.equals(this.owner);
    }

    public boolean matches(ClassNode clazz, MethodNode method) {
        return method.name.equals(this.name) && method.desc.equals(this.desc) && clazz.name.equals(this.owner);
    }
}
