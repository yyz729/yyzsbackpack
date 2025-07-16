package com.yyz.yyzsbackpack.mixin;


import com.yyz.yyzsbackpack.BackpackHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CompatMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.CreativeInventoryScreenMixin")) {
            return !BackpackHelper.isModLoaded("trinkets");
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.ItemEntityMixin")) {
            return !BackpackHelper.isModLoaded("collective");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.compat.collective.ItemEntityMixin")) {
            return BackpackHelper.isModLoaded("collective");
        }


        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.trinkets.SurvivalTrinketSlotMixin")) {
            return BackpackHelper.isModLoaded("trinkets") ;
        }

        if (mixinClassName.equals("com.yyz.yyzsbackpack.fabric.mixin.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackHelper.isModLoaded("hotbarslotcycling") ;
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.neoforge.mixin.hotbarslotcycling.HotbarCyclingProviderMixin")) {
            return BackpackHelper.isModLoaded("hotbarslotcycling") ;
        }

        return true;

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