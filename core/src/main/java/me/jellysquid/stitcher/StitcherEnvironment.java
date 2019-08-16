package me.jellysquid.stitcher;

import me.jellysquid.stitcher.annotations.Dist;

import java.util.Arrays;
import java.util.Collection;

public class StitcherEnvironment {
    private static final String CLIENT_CLASS_MARKER = "net/minecraft/client/Minecraft.class";

	public static final Dist DIST = StitcherEnvironment.isClientClassMarkerPresent() ? Dist.CLIENT : Dist.SERVER;

    private static boolean isClientClassMarkerPresent() {
        return StitcherEnvironment.class.getClassLoader().getResource(CLIENT_CLASS_MARKER) != null;
    }

    /**
     * This allows the discovery of additional plugins from the classpath that we may not otherwise be able
     * locate ourselves.
     *
     * @return A collection of strings representing the name of each plugin to load from the classpath.
     */
    public static Collection<String> getCommandLinePlugins() {
        return Arrays.asList(System.getProperty("stitcher.plugin.load", "").split(","));
    }

    public static boolean isDebuggingEnabled() {
        return System.getProperty("stitcher.debug", "false").equals("true");
    }

	public static boolean isTracingEnabled() {
		return System.getProperty("stitcher.trace", "false").equals("true");
	}
}
