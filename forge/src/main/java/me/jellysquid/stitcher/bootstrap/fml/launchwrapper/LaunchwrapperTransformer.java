package me.jellysquid.stitcher.bootstrap.fml.launchwrapper;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.environment.EnvironmentPatcher;
import net.minecraft.launchwrapper.IClassTransformer;

public class LaunchwrapperTransformer implements IClassTransformer {
    private final Stitcher stitcher;

    private final EnvironmentPatcher patcher;

    public LaunchwrapperTransformer() {
        this.stitcher = Stitcher.init(new LaunchwrapperEnvironment());
        this.patcher = this.stitcher.getPatcher();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return this.patcher.transform(name, basicClass);
    }
}
