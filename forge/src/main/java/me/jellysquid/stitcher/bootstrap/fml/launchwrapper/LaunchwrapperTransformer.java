package me.jellysquid.stitcher.bootstrap.fml.launchwrapper;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.environment.EnvironmentPatcher;
import net.minecraft.launchwrapper.IClassTransformer;

public class LaunchwrapperTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Stitcher stitcher = Stitcher.instance();

        EnvironmentPatcher patcher = stitcher.getPatcher();

		try {
            return patcher.transform(name, basicClass);
		} catch (Exception e) {
			Stitcher.LOGGER.fatal("An unexpected exception occurred while transforming class bytes! This is a critical error", e);

			throw new Error("Could not transform class bytes", e);
		}
    }
}
