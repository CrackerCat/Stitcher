package me.jellysquid.stitcher.plugin.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import me.jellysquid.stitcher.annotations.Dist;

import java.util.ArrayList;
import java.util.List;

public class PluginGroupConfig {
    private final String name;

    private final String packageRoot;

    private final Dist dist;

    private final List<String> transformers;

    public PluginGroupConfig(String name, JsonObject json) {
        this.name = name;
        this.packageRoot = json.get("package").asString();
        this.dist = Dist.fromName(json.getString("side", Dist.ANY.name()));

        JsonArray transformers = json.get("transformers").asArray();

        this.transformers = new ArrayList<>(transformers.size());

        for (JsonValue entry : transformers) {
            this.transformers.add(entry.asString());
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
