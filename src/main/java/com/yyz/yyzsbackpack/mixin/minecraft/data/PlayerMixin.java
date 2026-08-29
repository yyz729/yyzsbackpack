package com.yyz.yyzsbackpack.mixin.minecraft.data;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import com.yyz.yyzsbackpack.component.BackpackIdComponent;
import com.yyz.yyzsbackpack.component.ModComponents;
import com.yyz.yyzsbackpack.effect.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin implements IBackpackData {
    @Unique
    private static final EntityDataAccessor<ItemStack> DATA_BACKPACK_STACK =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.ITEM_STACK);
    @Unique
    private static final EntityDataAccessor<Integer> DATA_BACKPACK_INDEX =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<String> DATA_SELECTED_UUID =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);



    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void defineBackpackData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_BACKPACK_STACK, ItemStack.EMPTY);
        builder.define(DATA_BACKPACK_INDEX, 0);
        builder.define(DATA_SELECTED_UUID, "");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveBackpackData(ValueOutput output, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        output.putInt("BackpackSelectedIndex", player.getEntityData().get(DATA_BACKPACK_INDEX));
        output.store("BackpackItem", ItemStack.OPTIONAL_CODEC, player.getEntityData().get(DATA_BACKPACK_STACK));

        String uuid = player.getEntityData().get(DATA_SELECTED_UUID);
        if (!uuid.isEmpty()) output.putString("BackpackSelectedUuid", uuid);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadBackpackData(ValueInput input, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        int idx = input.getIntOr("BackpackSelectedIndex",0);
        Optional<ItemStack> optionalStack = input.read("BackpackItem", ItemStack.OPTIONAL_CODEC);
        player.getEntityData().set(DATA_BACKPACK_INDEX, idx);
        player.getEntityData().set(DATA_BACKPACK_STACK, optionalStack.orElse(ItemStack.EMPTY));

        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
        ((IExtendedInventory)player.getInventory()).yyzsbackpack$syncFromBackpack(selected);

        String uuid = input.getStringOr("BackpackSelectedUuid", "");
        if (!uuid.isEmpty()) player.getEntityData().set(DATA_SELECTED_UUID, uuid);
    }

    @Override
    public ItemStack yyzsbackpack$getSyncedBackpack() {
        return ((Player)(Object)this).getEntityData().get(DATA_BACKPACK_STACK);

    }

    @Override
    public void yyzsbackpack$setSyncedBackpack(ItemStack stack) {
        ((Player)(Object)this).getEntityData().set(DATA_BACKPACK_STACK, stack.copy());
    }

    @Override
    public int yyzsbackpack$getSyncedBackpackIndex() {
        return ((Player)(Object)this).getEntityData().get(DATA_BACKPACK_INDEX);
    }

    @Override
    public void yyzsbackpack$setSyncedBackpackIndex(int index) {
        ((Player)(Object)this).getEntityData().set(DATA_BACKPACK_INDEX, index);
    }

    @Override
    public String yyzsbackpack$getSelectedBackpackUuid() {
        return ((Player)(Object)this).getEntityData().get(DATA_SELECTED_UUID);
    }

    @Override
    public void yyzsbackpack$setSelectedBackpackUuid(String uuid) {
        ((Player)(Object)this).getEntityData().set(DATA_SELECTED_UUID, uuid == null ? "" : uuid);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        BackpackSlotHelper.updateWeightEffect(player);
        ((IExtendedInventory) player.getInventory()).yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));

        ItemStack current = BackpackSlotHelper.getSelectedBackpack(player);
        ItemStack synced = this.yyzsbackpack$getSyncedBackpack();

        boolean needUpdate = false;
        if (current.isEmpty() != synced.isEmpty()) {
            needUpdate = true;
        } else if (!current.isEmpty()) {
            BackpackIdComponent curId = current.get(ModComponents.BACKPACK_ID);
            BackpackIdComponent syncedId = synced.get(ModComponents.BACKPACK_ID);
            if (curId == null || syncedId == null || !curId.id().equals(syncedId.id())) {
                needUpdate = true;
            }
        }

        if (needUpdate) {
            this.yyzsbackpack$setSyncedBackpack(current);
        }
    }
}
