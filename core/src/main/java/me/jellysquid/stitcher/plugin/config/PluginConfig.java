package me.jellysquid.stitcher.plugin.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import me.jellysquid.stitcher.plugin.PluginManifest;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig {
    private final String name;

    private final String plugin;

    private final List<String> modules;

    public PluginConfig(PluginManifest manifest, JsonObject json) {
        this.name = manifest.getName();
        this.plugin = json.get("plugin").asString();

        JsonArray modules = json.get("modules").asArray();

        this.modules = new ArrayList<>(modules.size());

        for (JsonValue entry : modules) {
            this.modules.add(entry.asString());
        }
    }

    public String getName() {
        return this.name;
    }

    public String getPlugin() {
        return this.plugin;
    }

    public List<String> getModules() {
        return this.modules;
    }

}
