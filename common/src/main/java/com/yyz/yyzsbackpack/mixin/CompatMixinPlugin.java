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

        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.trinkets.SurvivalTrinketSlot")) {
            return BackpackPlatform.isModLoaded("trinkets");
        }
        if (mixinClassName.equals(" com.yyz.yyzsbackpack.fabric.mixin.compat.itemindicator.InventorySlotsIndicatorMixin")) {
            return BackpackPlatform.isModLoaded("durabilityviewer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.accessories.AccessoriesMenuMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.accessories.AccessoriesBasedSlotMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }


        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.curios.CuriosContainerMixin")) {
            return BackpackPlatform.isModLoaded("curios");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.curios.CuriosScreenMixin")) {
            return BackpackPlatform.isModLoaded("curios");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.curios.CurioSlotMixin")) {
            return BackpackPlatform.isModLoaded("curios");
        }
        if (mixinClassName.equals(" com.yyz.yyzsbackpack.neoforge.mixin.compat.itemindicator.InventorySlotsIndicatorMixin")) {
            return BackpackPlatform.isModLoaded("durabilityviewer");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.accessories.AccessoriesMenuMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.accessories.AccessoriesBasedSlotMixin")) {
            return BackpackPlatform.isModLoaded("accessories");
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackPlatform.isModLoaded("hotbarslotcycling") ;
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackPlatform.isModLoaded("hotbarslotcycling") ;
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.ItemEntityMixin")) {
            return !BackpackPlatform.isModLoaded("collective") || !BackpackPlatform.isFabric();
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.collective.ItemEntityMixin")) {
            return BackpackPlatform.isModLoaded("collective") && BackpackPlatform.isFabric();
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.compat.apothic_attributes.InventoryRenderMixin")) {
            return BackpackPlatform.isModLoaded("apothic_attributes");
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