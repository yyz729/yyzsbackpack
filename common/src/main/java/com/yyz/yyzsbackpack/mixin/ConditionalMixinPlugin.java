package com.yyz.yyzsbackpack.mixin;


import com.yyz.yyzsbackpack.BackpackHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ConditionalMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // 当需要应用 SomeMixin 时，检查目标模组是否加载
        if (mixinClassName.equals("com.yyz.yyzsbackpack.mixin.CreativeInventoryScreenMixin")) {
            return !BackpackHelper.isModLoaded("trinkets");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.CuriosContainerMixin")) {
            return BackpackHelper.isModLoaded("curios");
        }
        if (mixinClassName.equals("com.yyz.yyzsbackpack.forge.mixin.CuriosScreenMixin")) {
            return BackpackHelper.isModLoaded("curios");
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