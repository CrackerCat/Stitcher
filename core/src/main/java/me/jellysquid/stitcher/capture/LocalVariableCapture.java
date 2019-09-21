package me.jellysquid.stitcher.capture;

import me.jellysquid.stitcher.annotations.Capture;
import me.jellysquid.stitcher.util.AnnotationParser;
import me.jellysquid.stitcher.util.exceptions.TransformerBuildException;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalVariableCapture {
    private static final String CAPTURE_MARKER = Type.getDescriptor(Capture.class);

    private final List<CapturedVariable> inputs;

    public LocalVariableCapture(List<CapturedVariable> inputs) {
        this.inputs = inputs;
    }

    public static LocalVariableCapture buildCaptures(MethodNode methodNode) throws TransformerBuildException {
        if (methodNode.invisibleParameterAnnotations == null || methodNode.invisibleParameterAnnotations.length == 0) {
            return new LocalVariableCapture(Collections.emptyList());
        }

        List<CapturedVariable> inputs = new ArrayList<>();

        List<AnnotationNode>[] annotations = methodNode.invisibleParameterAnnotations;

        Type[] types = Type.getArgumentTypes(methodNode.desc);

        for (int i = 0; i < annotations.length; i++) {
            List<AnnotationNode> methodParameterAnnotations = annotations[i];

            if (methodParameterAnnotations != null && !methodParameterAnnotations.isEmpty()) {
                Type type = types[i];

                for (AnnotationNode parameterAnnotation : methodParameterAnnotations) {
                    if (!parameterAnnotation.desc.equals(CAPTURE_MARKER)) {
                        continue;
                    }

                    AnnotationParser values = new AnnotationParser(parameterAnnotation);

                    int var = values.getValue("index", Integer.class);

                    if (var == 0) {
                        throw new TransformerBuildException("Local variable at index 0 cannot be captured");
                    } else if (var < 0) {
                        throw new TransformerBuildException("Local variable index must be positive");
                    }

                    inputs.add(new CapturedVariable(type, var));
                }
            }
        }

        return new LocalVariableCapture(inputs);
    }

    public InsnList createLoadInstructions(MethodNode methodNode) throws TransformerException {
        InsnList list = new InsnList();

        for (CapturedVariable var : this.inputs) {
            list.add(var.createLoadInstruction(methodNode));
        }

        return list;
    }
}
