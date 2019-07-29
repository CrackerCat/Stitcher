package me.jellysquid.stitcher.bootstrap.fml.launchwrapper;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.environment.EnvironmentPatcher;
import net.minecraft.launchwrapper.IClassTransformer;

public class LaunchwrapperTransformer implements IClassTransformer {
    private final EnvironmentPatcher patcher;

    public LaunchwrapperTransformer() {
        this.patcher = Stitcher.instance().getPatcher();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return this.patcher.transform(name, basicClass);
    }
}
