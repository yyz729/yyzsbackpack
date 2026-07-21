package com.yyz.yyzsbackpack.mixin.minecraft.data;

import com.mojang.logging.LogUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin {


    @Redirect(method = "defineId", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;verifyEntityDataAccessorRegistration(Ljava/lang/Class;Ljava/lang/Class;)V"))
    private static void bypassVerification(Class<?> callerClass, Class<? extends SyncedDataHolder> entityClass) {
        // 不执行任何检查，直接放行
    }
}