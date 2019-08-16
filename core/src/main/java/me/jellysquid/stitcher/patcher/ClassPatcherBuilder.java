package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.annotations.*;
import me.jellysquid.stitcher.plugin.Plugin;
import me.jellysquid.stitcher.plugin.config.PluginGroupConfig;
import me.jellysquid.stitcher.remap.Remapper;
import me.jellysquid.stitcher.transformers.MethodInjectionTransformer;
import me.jellysquid.stitcher.transformers.MethodOverwriteTransformer;
import me.jellysquid.stitcher.transformers.MethodRedirectTransformer;
import me.jellysquid.stitcher.transformers.MethodVariableTransformer;
import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPatcherBuilder {
    private final Map<String, ClassTransformerFactory> methodTransformationTypes = new HashMap<>();

    public ClassPatcherBuilder() {
        this.registerAnnotationConsumer(Type.getDescriptor(Overwrite.class), new MethodOverwriteTransformer.Builder());
        this.registerAnnotationConsumer(Type.getDescriptor(Inject.class), new MethodInjectionTransformer.Builder());
        this.registerAnnotationConsumer(Type.getDescriptor(Redirect.class), new MethodRedirectTransformer.Builder());
        this.registerAnnotationConsumer(Type.getDescriptor(ModifyVariable.class), new MethodVariableTransformer.Builder());
    }

    private void registerAnnotationConsumer(String type, ClassTransformerFactory factory) {
        this.methodTransformationTypes.put(type, factory);
    }

    public ClassPatcher createClassPatcher(Plugin plugin, PluginGroupConfig config, String className) {
        byte[] classBytes;

        try {
            classBytes = plugin.getResources().getBytes(className);
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch bytecode to construct class patcher", e);
        }

        ClassNode classNode = new ClassNode();

        ClassReader classReader = new ClassReader(classBytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

        try {
            return this.buildClassPatcher(plugin, config, classNode);
        } catch (Exception e) {
            throw new RuntimeException("Could not build class patcher from class " + className, e);
        }
    }

    private static final String TRANSFORM_MARKER = Type.getDescriptor(Transform.class);

    private ClassPatcher buildClassPatcher(Plugin plugin, PluginGroupConfig config, ClassNode classNode) {
        Type target = null;

        for (AnnotationNode annotation : classNode.invisibleAnnotations) {
            if (annotation.desc.equals(TRANSFORM_MARKER)) {
                AnnotationParser values = new AnnotationParser(annotation);
                target = values.getValue("value", Type.class);

                break;
            }
        }

        if (target == null) {
            throw new RuntimeException("No transform target annotated on root class");
        }

        Remapper remapper = new Remapper(target);
        remapper.addFlattenedClass(classNode);

        ClassPatcher patcher = new ClassPatcher("plugin[" + plugin.getConfig().getName() + "]://" + classNode.name, target);

        for (String interfaceName : classNode.interfaces) {
            patcher.addInterface(interfaceName);
        }

        for (FieldNode fieldNode : classNode.fields) {
            if (!remapper.registerFieldMapping(fieldNode)) {
                patcher.addField(fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>")) {
                continue;
            }

            ClassTransformer transformer = this.buildMethodTransformer(config, methodNode);

            if (transformer != null) {
                patcher.addTransformer(transformer);
            } else if (!remapper.registerMethodMapping(methodNode)) {
                patcher.addMethod(methodNode);
            }
        }

        remapper.transform(classNode);

        return patcher;
    }

    private ClassTransformer buildMethodTransformer(PluginGroupConfig config, MethodNode methodNode) {
        List<AnnotationNode> annotations = methodNode.invisibleAnnotations;

        if (annotations == null || annotations.isEmpty()) {
            return null;
        }

        ClassTransformer methodTransformer = null;

        for (AnnotationNode annotation : methodNode.invisibleAnnotations) {
            ClassTransformerFactory factory = this.methodTransformationTypes.get(annotation.desc);

            if (factory != null) {
                try {
                    methodTransformer = factory.build(config, methodNode, annotation);
                } catch (me.jellysquid.stitcher.util.exceptions.TransformerBuildException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        return methodTransformer;
    }

}
