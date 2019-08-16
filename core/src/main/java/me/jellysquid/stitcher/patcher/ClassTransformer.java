package me.jellysquid.stitcher.patcher;

import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;

public abstract class ClassTransformer {
	protected int priority;

	protected ClassTransformer() {
		this.priority = 0;
	}

	protected ClassTransformer(int priority) {
		this.priority = priority;
	}

	public abstract boolean transform(ClassNode classNode) throws TransformerException;

	public final int getPriority() {
		return this.priority;
	}
}
