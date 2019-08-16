package me.jellysquid.stitcher.transformers;

import me.jellysquid.stitcher.patcher.ClassTransformer;
import me.jellysquid.stitcher.util.exceptions.TransformerException;
import org.objectweb.asm.tree.ClassNode;

public class ClassInterfaceTransformer extends ClassTransformer {
	private final String interfaceName;

	public ClassInterfaceTransformer(String interfaceName, int priority) {
		super(priority);

		this.interfaceName = interfaceName;
	}

	@Override
	public boolean transform(ClassNode classNode) throws TransformerException {
		classNode.interfaces.add(this.interfaceName);

		return true;
	}

	@Override
	public String toString() {
		return String.format("ClassInterfaceTransformer{name='%s'}", this.interfaceName);
	}
}
