// 假设类名为 ModComponents
package com.yyz.yyzsbackpack.component;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.component.BackpackIdComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Backpack.MOD_ID);

    public static final Supplier<DataComponentType<BackpackIdComponent>> BACKPACK_ID =
            DATA_COMPONENTS.registerComponentType(
                    "backpack_id",
                    builder -> builder
                            .persistent(BackpackIdComponent.CODEC)
                            .networkSynchronized(BackpackIdComponent.STREAM_CODEC)
            );
}