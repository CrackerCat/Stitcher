package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.annotations.*;
import me.jellysquid.stitcher.plugin.PluginResource;
import me.jellysquid.stitcher.remap.Remapper;
import me.jellysquid.stitcher.transformers.*;
import me.jellysquid.stitcher.transformers.factory.ReflectionClassTransformerFactory;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformationReader {
    private final Map<String, ClassTransformerFactory> methodTransformationTypes = new HashMap<>();

    public TransformationReader() {
        this.registerType(Type.getDescriptor(Overwrite.class), new ReflectionClassTransformerFactory(MethodOverwriteTransformer.class));
        this.registerType(Type.getDescriptor(Inject.class), new ReflectionClassTransformerFactory(MethodInjectionTransformer.class));
        this.registerType(Type.getDescriptor(Redirect.class), new ReflectionClassTransformerFactory(MethodRedirectTransformer.class));
        this.registerType(Type.getDescriptor(ModifyVariable.class), new ReflectionClassTransformerFactory(MethodVariableTransformer.class));
    }

    private void registerType(String type, ClassTransformerFactory factory) {
        this.methodTransformationTypes.put(type, factory);
    }

    public TransformationData readTransformations(PluginResource resource) {
        byte[] classBytes;

        try {
            classBytes = resource.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch bytecode to construct class patcher", e);
        }

        ClassNode classNode = new ClassNode();

        ClassReader classReader = new ClassReader(classBytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

        try {
            return this.readTransformations(resource, classNode);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not build class patcher from class %s", resource.getPath()), e);
        }
    }

    private static final String TRANSFORM_MARKER = Type.getDescriptor(Transform.class);

    private TransformationData readTransformations(PluginResource source, ClassNode classNode) {
        Type target = null;
        int priority = 0;

        for (AnnotationNode annotation : classNode.invisibleAnnotations) {
            if (annotation.desc.equals(TRANSFORM_MARKER)) {
                AnnotationParser values = new AnnotationParser(annotation);
                target = values.getValue("value", Type.class);
                priority = values.getValue("priority", Integer.class, 0);

                break;
            }
        }

        if (target == null) {
            throw new RuntimeException("No transform target annotated on root class (did you specify an unrelated class name?)");
        }

        Remapper remapper = new Remapper(target);
        remapper.addFlattenedClass(classNode);

        List<ClassTransformer> transformers = new ArrayList<>();

        for (String interfaceName : classNode.interfaces) {
            transformers.add(new ClassInterfaceTransformer(source, interfaceName, priority));
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (!remapper.registerFieldMapping(fieldNode)) {
                transformers.add(new ClassFieldTransformer(source, fieldNode, priority));
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>")) {
                continue;
            }

            ClassTransformer transformer = this.buildMethodTransformer(source, methodNode);

            if (transformer != null) {
                transformers.add(transformer);
            } else if (!remapper.registerMethodMapping(methodNode)) {
                transformers.add(new ClassMethodTransformer(source, methodNode, priority));
            }
        }

        remapper.transform(classNode);

        if (transformers.isEmpty()) {
            return null;
        }

        return new TransformationData(target, transformers);
    }

    private ClassTransformer buildMethodTransformer(PluginResource source, MethodNode methodNode) {
        List<AnnotationNode> annotations = methodNode.invisibleAnnotations;

        if (annotations == null || annotations.isEmpty()) {
            return null;
        }

        ClassTransformer methodTransformer = null;

        for (AnnotationNode annotation : methodNode.invisibleAnnotations) {
            ClassTransformerFactory factory = this.methodTransformationTypes.get(annotation.desc);

            if (factory != null) {
                try {
                    methodTransformer = factory.build(source, methodNode, annotation);
                } catch (TransformerBuildException e) {
                    throw new RuntimeException("Failed to build method transformer", e);
                }

                break;
            }
        }

        return methodTransformer;
    }

}
