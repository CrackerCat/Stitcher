package me.jellysquid.stitcher.remap;

import me.jellysquid.stitcher.annotations.Shadow;
import me.jellysquid.stitcher.annotations.Transform;
import me.jellysquid.stitcher.inject.slice.SliceRange;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Remapper {
    private static final String MARKER_TRANSFORM_CLASS = Type.getDescriptor(Transform.class);
    private static final String MARKER_SHADOW_FIELD = Type.getDescriptor(Shadow.class);

    private final HashSet<String> flattenedClasses = new HashSet<>();
    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, String> methods = new HashMap<>();

    public final String target;

    public Remapper(Type target) {
        this.target = target.getInternalName();
    }

    public void addFlattenedClass(ClassNode classNode) {
        this.flattenedClasses.add(classNode.name);
    }

    public boolean registerFieldMapping(FieldNode fieldNode) {
        if (fieldNode.invisibleAnnotations == null) {
            return false;
        }

        for (AnnotationNode annotationNode : fieldNode.invisibleAnnotations) {
            if (annotationNode.desc.equals(MARKER_SHADOW_FIELD)) {
                AnnotationParser values = new AnnotationParser(annotationNode);

                this.fields.put(fieldNode.name, values.getValue("value", String.class));

                return true;
            }
        }

        return false;
    }

    public boolean registerMethodMapping(MethodNode methodNode) {
        if (methodNode.invisibleAnnotations == null) {
            return false;
        }

        for (AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
            if (annotationNode.desc.equals(MARKER_SHADOW_FIELD)) {
                AnnotationParser values = new AnnotationParser(annotationNode);

                this.methods.put(methodNode.name + methodNode.desc, values.getValue("value", String.class));

                return true;
            }
        }

        return false;
    }

    public void transform(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            this.transform(methodNode);
        }
    }

    private void transform(MethodNode methodNode) {
        for (AbstractInsnNode node : SliceRange.all(methodNode.instructions)) {
            if (node.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) node;

                String remapped = this.fields.get(fieldInsnNode.name);

                if (remapped != null) {
                    fieldInsnNode.owner = this.target;
                    fieldInsnNode.name = remapped;
                } else if (this.flattenedClasses.contains(fieldInsnNode.owner)) {
                    fieldInsnNode.owner = this.target;
                }
            } else if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) node;

                String remapped = this.methods.get(methodInsnNode.name + methodInsnNode.desc);

                if (remapped != null) {
                    methodInsnNode.owner = this.target;
                    methodInsnNode.name = remapped;
                } else if (this.flattenedClasses.contains(methodInsnNode.owner)) {
                    methodInsnNode.owner = this.target;
                }
            }
        }
    }


}
