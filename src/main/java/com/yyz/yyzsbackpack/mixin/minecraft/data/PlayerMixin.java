package com.yyz.yyzsbackpack.mixin.minecraft.data;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Unique
    Player player = (Player) (Object) this;

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void defineBackpackData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_BACKPACK_STACK, ItemStack.EMPTY);
        builder.define(DATA_BACKPACK_INDEX, 0);
        builder.define(DATA_SELECTED_UUID, "");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveBackpackData(CompoundTag compoundTag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        compoundTag.putInt("BackpackSelectedIndex", player.getEntityData().get(DATA_BACKPACK_INDEX));

        // 保存 ItemStack
        compoundTag.put("BackpackItem", player.getEntityData().get(DATA_BACKPACK_STACK).save(player.registryAccess()));

        String uuid = player.getEntityData().get(DATA_SELECTED_UUID);
        if (!uuid.isEmpty()) {
            compoundTag.putString("BackpackSelectedUuid", uuid);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadBackpackData(CompoundTag compoundTag, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        int idx = compoundTag.contains("BackpackSelectedIndex") ? compoundTag.getInt("BackpackSelectedIndex") : 0;
        player.getEntityData().set(DATA_BACKPACK_INDEX, idx);

        ItemStack stack = ItemStack.EMPTY;
        if (compoundTag.contains("BackpackItem")) {
            stack = ItemStack.parse(player.registryAccess(), compoundTag.getCompound("BackpackItem"))
                    .orElse(ItemStack.EMPTY);
        }
        player.getEntityData().set(DATA_BACKPACK_STACK, stack);

        // 读取后同步背包
        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
        ((IExtendedInventory) player.getInventory()).yyzsbackpack$syncFromBackpack(selected);

        String uuid = compoundTag.getString("BackpackSelectedUuid"); // 不存在返回 ""
        if (!uuid.isEmpty()) {
            player.getEntityData().set(DATA_SELECTED_UUID, uuid);
        }
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
        BackpackSlotHelper.updateWeightEffect(player);
        ((IExtendedInventory) player.getInventory()).yyzsbackpack$syncFromBackpack(BackpackSlotHelper.getSelectedBackpack(player));
    }
}
