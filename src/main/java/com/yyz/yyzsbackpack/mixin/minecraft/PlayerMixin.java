package com.yyz.yyzsbackpack.mixin.minecraft;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

@Mixin(Player.class)
public abstract class PlayerMixin implements IBackpackData {
    @Unique
    private static final EntityDataAccessor<ItemStack> DATA_BACKPACK_STACK =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.ITEM_STACK);
    @Unique
    private static final EntityDataAccessor<Integer> DATA_BACKPACK_INDEX =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void defineBackpackData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_BACKPACK_STACK, ItemStack.EMPTY);
        builder.define(DATA_BACKPACK_INDEX, 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveBackpackData(ValueOutput output, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        output.putInt("BackpackSelectedIndex", player.getEntityData().get(DATA_BACKPACK_INDEX));
        output.store("BackpackItem", ItemStack.CODEC, player.getEntityData().get(DATA_BACKPACK_STACK));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadBackpackData(ValueInput input, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        int idx = input.getIntOr("BackpackSelectedIndex",0);
        Optional<ItemStack> optionalStack = input.read("BackpackItem", ItemStack.CODEC);
        player.getEntityData().set(DATA_BACKPACK_INDEX, idx);
        player.getEntityData().set(DATA_BACKPACK_STACK, optionalStack.orElse(ItemStack.EMPTY));

        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(player);
        ((IExtendedInventory)player.getInventory()).yyzsbackpack$syncFromBackpack(selected);
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
}
