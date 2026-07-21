package com.yyz.yyzsbackpack.network.packets.data;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.data.BackpackData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record BackpackDataSyncS2CPacket(Map<String, BackpackData> dataMap) {
    public static final ResourceLocation ID = new ResourceLocation(Backpack.MOD_ID, "backpack_data_sync");

    public static void write(FriendlyByteBuf buf, BackpackDataSyncS2CPacket packet) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, BackpackData> entry : packet.dataMap().entrySet()) {
            // 假设 BackpackData 实现了 toNbt() 方法，若无请替换为你的序列化逻辑
            tag.put(entry.getKey(), entry.getValue().toNbt());
        }
        buf.writeNbt(tag);
    }

    public static BackpackDataSyncS2CPacket read(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        Map<String, BackpackData> map = new HashMap<>();
        if (tag != null) {
            for (String key : tag.getAllKeys()) {
                CompoundTag dataTag = tag.getCompound(key);
                // 假设 BackpackData 有静态 fromNbt 方法
                BackpackData data = BackpackData.fromNbt(dataTag);
                map.put(key, data);
            }
        }
        return new BackpackDataSyncS2CPacket(map);
    }
}