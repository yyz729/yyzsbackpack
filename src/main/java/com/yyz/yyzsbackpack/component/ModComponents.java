// 假设类名为 ModComponents
package com.yyz.yyzsbackpack.component;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.component.BackpackIdComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModComponents {
    public static final DataComponentType<BackpackIdComponent> BACKPACK_ID =
            DataComponentType.<BackpackIdComponent>builder()
                    .persistent(BackpackIdComponent.CODEC)
                    .networkSynchronized(BackpackIdComponent.STREAM_CODEC)
                    .build();

    public static void register() {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(Backpack.MOD_ID, "backpack_id"),
                BACKPACK_ID
        );
    }
}