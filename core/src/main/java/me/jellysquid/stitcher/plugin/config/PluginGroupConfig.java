package me.jellysquid.stitcher.plugin.config;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import me.jellysquid.stitcher.annotations.Dist;

import java.util.ArrayList;
import java.util.List;

public class PluginGroupConfig {
    private final String name;

    private final String packageRoot;

    private final Dist dist;

	private final List<String> transformers = new ArrayList<>();

    public PluginGroupConfig(String name, JsonObject json) {
        this.name = name;
        this.packageRoot = json.get("package").asString();

        this.dist = Dist.fromName(json.getString("side", Dist.ANY.name()));

		JsonValue transformers = json.get("transformers");

		if (transformers != null) {
			for (JsonValue entry : transformers.asArray()) {
				this.transformers.add(entry.asString());
			}
        }
    }

    public String getPackageRoot() {
        return this.packageRoot;
    }

    public Dist getDist() {
        return this.dist;
    }

    public List<String> getTransformers() {
        return this.transformers;
    }

    public String getName() {
        return this.name;
    }
}
