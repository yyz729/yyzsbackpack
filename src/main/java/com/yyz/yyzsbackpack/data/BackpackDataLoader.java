package com.yyz.yyzsbackpack.data;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

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

    private static void apply(Map<Identifier, BackpackData> map, ResourceManager manager, ProfilerFiller profiler) {
        DATA.clear();
        for (BackpackData data : map.values()) {
            DATA.put(data.type(), data);
        }
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener<BackpackData> {
        public ReloadListener() {
            super(
                    BackpackData.CODEC,
                    FileToIdConverter.json("backpacks")
            );
        }

        @Override
        protected void apply(Map<Identifier, BackpackData> prepared,
                             @NonNull ResourceManager manager,
                             @NonNull ProfilerFiller profiler) {
            BackpackDataLoader.apply(prepared, manager, profiler);
        }
    }
}