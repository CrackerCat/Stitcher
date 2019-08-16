package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassMethodTransformer extends ClassTransformer {
	private final MethodNode method;

	public ClassMethodTransformer(MethodNode method, int priority) {
		super(priority);

		this.method = method;
	}

	@Override
	public boolean transform(ClassNode classNode) throws TransformerException {
		classNode.methods.add(this.method);

		return true;
	}

	@Override
	public String toString() {
		return String.format("ClassMethodTransformer{name='%s'}", this.method.name);
	}
}
