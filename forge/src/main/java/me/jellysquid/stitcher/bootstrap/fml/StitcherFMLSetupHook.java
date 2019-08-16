package me.jellysquid.stitcher.bootstrap.fml;

import me.jellysquid.stitcher.Stitcher;
import me.jellysquid.stitcher.bootstrap.fml.launchwrapper.LaunchwrapperEnvironment;
import net.minecraftforge.fml.relauncher.IFMLCallHook;

import java.util.Map;

public class StitcherFMLSetupHook implements IFMLCallHook {
    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public Void call() throws Exception {
        Stitcher.init(new LaunchwrapperEnvironment());

        return null;
    }
}
