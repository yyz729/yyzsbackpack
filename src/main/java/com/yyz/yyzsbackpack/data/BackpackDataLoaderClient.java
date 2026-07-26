package com.yyz.yyzsbackpack.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class BackpackDataLoaderClient {
    private static final Map<String, BackpackData> CLIENT_DATA = new HashMap<>();

    /**
     * 由 ReloadListener.apply 调用，将解析后的数据存入 CLIENT_DATA
     */
    private static void apply(Map<ResourceLocation, BackpackData> map, ResourceManager manager, ProfilerFiller profiler) {
        CLIENT_DATA.clear();
        for (BackpackData data : map.values()) {
            CLIENT_DATA.put(data.type(), data);
        }
        Backpack.LOGGER.info("Loaded {} client backpack definitions", CLIENT_DATA.size());
    }

    /**
     * 将服务端同步的数据与客户端本地数据合并
     */
    public static Map<String, BackpackData> merge(Map<String, BackpackData> serverData) {
        Map<String, BackpackData> merged = new HashMap<>();

        for (Map.Entry<String, BackpackData> entry : serverData.entrySet()) {
            String type = entry.getKey();
            BackpackData serverEntry = entry.getValue();

            if (serverEntry.forceServer()) {
                merged.put(type, serverEntry);
            } else {
                BackpackData clientEntry = CLIENT_DATA.get(type);
                if (clientEntry != null) {
                    BackpackData combined = new BackpackData(
                            clientEntry.type(),
                            serverEntry.size(),
//                            clientEntry.guiTexture(),
//                            clientEntry.backgroundX(),
//                            clientEntry.backgroundY(),
                            false,
                            clientEntry.maxVisibleTabs(),
                            clientEntry.segments()

                    );
                    merged.put(type, combined);
                } else {
                    Backpack.LOGGER.warn("Missing client backpack data for type '{}', falling back to server data.", type);
                    merged.put(type, serverEntry);
                }
            }
        }
        return merged;
    }

    /**
     * 客户端重载监听器
     */
    public static class ReloadListener extends SimpleJsonResourceReloadListener{

        public ReloadListener(ResourceLocation id) {
            super(new Gson(), "backpacks");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> prepared,
                             ResourceManager manager,
                             ProfilerFiller profiler) {
            CLIENT_DATA.clear();
            for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
                BackpackData.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> Backpack.LOGGER.error("Failed to parse client backpack data {}: {}", entry.getKey(), error))
                        .ifPresent(data -> CLIENT_DATA.put(data.type(), data));
            }
        }
    }
}