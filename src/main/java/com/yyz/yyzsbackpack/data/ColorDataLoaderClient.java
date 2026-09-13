package com.yyz.yyzsbackpack.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yyz.yyzsbackpack.Backpack;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ColorDataLoaderClient {

    private static final Gson GSON = new Gson();

    private static final Map<String, Integer> DEFAULT_COLORS = new HashMap<>();

    private static final int FALLBACK = -6265536;

    public static int getDefaultColor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (!id.getNamespace().equals(Backpack.MOD_ID)) {
            return FALLBACK;
        }
        return DEFAULT_COLORS.getOrDefault(id.getPath(), FALLBACK);
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
        private final ResourceLocation id;

        public ReloadListener(ResourceLocation id) {
            super(GSON, "items");
            this.id = id;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
            DEFAULT_COLORS.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                if (!location.getNamespace().equals(Backpack.MOD_ID)) continue;

                try {
                    int color = parseDefaultDyeColor(entry.getValue().getAsJsonObject());
                    if (color != Integer.MIN_VALUE) {
                        DEFAULT_COLORS.put(location.getPath(), color);
                    }
                } catch (Exception e) {
                    Backpack.LOGGER.error("Failed to parse default dye color from {}: {}", location, e.getMessage());
                }
            }

            Backpack.LOGGER.info("Loaded {} backpack default dye colors from items/*.json", DEFAULT_COLORS.size());
        }

        private int parseDefaultDyeColor(JsonObject root) {
            if (!root.has("model")) return Integer.MIN_VALUE;

            JsonObject model = root.getAsJsonObject("model");
            if (!model.has("tints") || !model.get("tints").isJsonArray()) {
                return Integer.MIN_VALUE;
            }

            JsonArray tints = model.getAsJsonArray("tints");
            if (tints.isEmpty()) return Integer.MIN_VALUE;

            // 取第一个 tint（对应 tintIndex 0）
            JsonObject firstTint = tints.get(0).getAsJsonObject();
            if (!firstTint.has("type") || !firstTint.has("default")) {
                return Integer.MIN_VALUE;
            }

            String type = firstTint.get("type").getAsString();

            if (!type.equals("minecraft:dye") && !type.equals("dye")) {
                return Integer.MIN_VALUE;
            }

            return firstTint.get("default").getAsInt();
        }

        @Override
        public ResourceLocation getFabricId() {
            return id;
        }
    }
}
