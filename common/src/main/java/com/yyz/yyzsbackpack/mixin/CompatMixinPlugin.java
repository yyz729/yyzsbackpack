package com.yyz.yyzsbackpack.mixin;

import com.yyz.yyzsbackpack.BackpackPlatform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CompatMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // 当需要应用 SomeMixin 时，检查目标模组是否加载
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.CreativeInventoryScreenMixin")) {
            return !BackpackPlatform.isModLoaded("trinkets");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.SurvivalTrinketSlot")) {
            return BackpackPlatform.isModLoaded("trinkets");
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.ItemEntityMixin")) {
            return !BackpackPlatform.isModLoaded("collective") || !BackpackPlatform.isFabric();
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.collective.ItemEntityMixin")) {
            return BackpackPlatform.isModLoaded("collective") && BackpackPlatform.isFabric();
        }


        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.compat.curios.CuriosContainerMixin")) {
            return BackpackPlatform.isModLoaded("curios") && !BackpackPlatform.isModLoaded("cclayer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.compat.curios.CuriosContainerV2Mixin")) {
            return BackpackPlatform.isModLoaded("curios") && !BackpackPlatform.isModLoaded("cclayer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.compat.curios.CuriosScreenMixin")) {
            return BackpackPlatform.isModLoaded("curios") && !BackpackPlatform.isModLoaded("cclayer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.compat.curios.CuriosScreenV2Mixin")) {
            return BackpackPlatform.isModLoaded("curios") && !BackpackPlatform.isModLoaded("cclayer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.compat.curios.CurioSlotMixin")) {
            return BackpackPlatform.isModLoaded("curios") && !BackpackPlatform.isModLoaded("cclayer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.BackpackInventoryScreenMixin")) {
            return BackpackPlatform.isModLoaded("quark");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.TotemOfHoldingEntity")) {
            return BackpackPlatform.isModLoaded("quark");
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.accessories.AccessoriesMenuMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.accessories.AccessoriesBasedSlotMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }


        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.emi.CookingRecipeHandlerMixin")) {
            return BackpackPlatform.isModLoaded("emi");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.emi.CraftingRecipeHandlerMixin")) {
            return BackpackPlatform.isModLoaded("emi");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.emi.InventoryRecipeHandlerMixin")) {
            return BackpackPlatform.isModLoaded("emi");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.compat.emi.StonecuttingRecipeHandlerMixin")) {
            return BackpackPlatform.isModLoaded("emi");
        }

        if (mixinClassName.equals(" com.yyz.yyzsbackpack.fabric.mixin.InventorySlotsIndicatorMixin")) {
            return BackpackPlatform.isModLoaded("durabilityviewer");
        }

        if (mixinClassName.equals(" com.yyz.yyzsbackpack.forge.mixin.InventorioScreenHandler")) {
            return BackpackPlatform.isModLoaded("inventorio");
        }
        if (mixinClassName.equals(" com.yyz.yyzsbackpack.forge.mixin.InventorioScreen")) {
            return BackpackPlatform.isModLoaded("inventorio");
        }
        if (mixinClassName.equals(" com.yyz.yyzsbackpack.fabric.mixin.InventorioScreenHandler")) {
            return BackpackPlatform.isModLoaded("inventorio");
        }
        if (mixinClassName.equals(" com.yyz.yyzsbackpack.fabric.mixin.InventorioScreen")) {
            return BackpackPlatform.isModLoaded("inventorio");
        }


        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackPlatform.isModLoaded("slotcycler") ;
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackPlatform.isModLoaded("slotcycler") ;
        }

        return true; // 默认启用其他 Mixin

    }

    // 以下方法留空即可
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}