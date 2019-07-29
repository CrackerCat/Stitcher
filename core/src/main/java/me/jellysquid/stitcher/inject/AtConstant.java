package me.jellysquid.stitcher.inject;

import me.jellysquid.stitcher.util.AnnotationParser;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AtConstant extends InstructionMatcher {
    private final Object data;

    public AtConstant(AnnotationParser annotation) {
        String extra = annotation.getValue("constant", String.class);

        this.data = AtConstant.parse(extra);
    }

    private static Object parse(String text) {
        String[] components = text.split(":");

        if (components.length != 2) {
            throw new IllegalArgumentException("Expected type:value pair, found " + text);
        }

        String type = components[0];
        String value = components[1];

        switch (type) {
            case "string":
                return value;
            case "int":
                return Integer.valueOf(value);
            case "float":
                return Float.valueOf(value);
            case "long":
                return Long.valueOf(value);
            case "double":
                return Double.valueOf(value);
            default:
                throw new IllegalArgumentException("Unsupported constant type: " + type);
        }
    }

    @Override
    public boolean matches(MethodNode method, AbstractInsnNode node) {
        if (node.getType() == AbstractInsnNode.LDC_INSN) {
            return ((LdcInsnNode) node).cst.equals(this.data);
        }

        return false;
    }
}
