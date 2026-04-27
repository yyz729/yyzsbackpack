package com.yyz.yyzsbackpack.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record BackupRecord(long timestamp, List<ItemStack> items) {

    // 定义 Codec，用于序列化/反序列化
    public static final Codec<BackupRecord> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("timestamp").forGetter(BackupRecord::timestamp),
                    Codec.list(ItemStack.OPTIONAL_CODEC).fieldOf("items").forGetter(BackupRecord::items)
            ).apply(instance, BackupRecord::new)
    );

    // 便捷构造：自动深拷贝 items
    public BackupRecord(long timestamp, List<ItemStack> items) {
        this.timestamp = timestamp;
        this.items = items.stream()
                .map(ItemStack::copy)
                .toList(); // 不可变列表，防止外部修改
    }
}