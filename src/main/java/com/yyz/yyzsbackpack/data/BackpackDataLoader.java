package com.yyz.yyzsbackpack.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BackpackDataLoader {
    private static final Map<String, BackpackData> DATA = new HashMap<>();

    public static BackpackData getData(String type) {
        return DATA.get(type);
    }

    // 服务端发送全部数据
    public static Map<String, BackpackData> getAllData() {
        return Collections.unmodifiableMap(DATA);
    }

    // 客户端接收数据时填充
    public static void setData(Map<String, BackpackData> newData) {
        DATA.clear();
        DATA.putAll(newData);
    }

    private static void apply(Map<ResourceLocation, BackpackData> map, ResourceManager manager, ProfilerFiller profiler) {
        DATA.clear();
        for (BackpackData data : map.values()) {
            DATA.put(data.type(), data);
        }
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener{
        private final ResourceLocation id;
        public ReloadListener(ResourceLocation id) {
            super(new Gson(), "backpacks");
            this.id = id;
        }


        @Override
        protected void apply(Map<ResourceLocation, JsonElement> prepared,
                             @NotNull ResourceManager manager,
                             @NotNull ProfilerFiller profiler) {
            Map<String, BackpackData> newData = new HashMap<>();

            for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
                ResourceLocation id = entry.getKey();
                JsonElement json = entry.getValue();

                BackpackData.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> Backpack.LOGGER.error("Failed to parse backpack data {}: {}", id, error))
                        .ifPresent(data -> newData.put(data.type(), data));
            }

            // 替换全局数据
            setData(newData);
        }
    }
}