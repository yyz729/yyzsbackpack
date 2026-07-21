package com.yyz.yyzsbackpack.mixin.minecraft.data;

import com.yyz.yyzsbackpack.api.IExtendedInventory;
import com.yyz.yyzsbackpack.api.IBackpackData;
import com.yyz.yyzsbackpack.api.helper.BackpackSlotHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IBackpackData {
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

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void defineBackpackData(CallbackInfo ci) {
        this.entityData.define(DATA_BACKPACK_STACK, ItemStack.EMPTY);
        this.entityData.define(DATA_BACKPACK_INDEX, 0);
        this.entityData.define(DATA_SELECTED_UUID, "");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveBackpackData(CompoundTag compoundTag, CallbackInfo ci) {
        // 保存索引
        compoundTag.putInt("BackpackSelectedIndex", this.player.getEntityData().get(DATA_BACKPACK_INDEX));

        // 保存 UUID
        String uuid = this.player.getEntityData().get(DATA_SELECTED_UUID);
        if (!uuid.isEmpty()) {
            compoundTag.putString("BackpackSelectedUuid", uuid);
        }

        // 保存背包物品
        ItemStack stack = this.player.getEntityData().get(DATA_BACKPACK_STACK);
        if (!stack.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            compoundTag.put("BackpackItem", itemTag);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadBackpackData(CompoundTag compoundTag, CallbackInfo ci) {
        // 读取索引
        int idx = compoundTag.contains("BackpackSelectedIndex") ?
                compoundTag.getInt("BackpackSelectedIndex") : 0;
        this.player.getEntityData().set(DATA_BACKPACK_INDEX, idx);

        // 读取 UUID
        String uuid = compoundTag.getString("BackpackSelectedUuid");
        if (!uuid.isEmpty()) {
            this.player.getEntityData().set(DATA_SELECTED_UUID, uuid);
        }

        //读取背包物品
        ItemStack stack = ItemStack.EMPTY;
        if (compoundTag.contains("BackpackItem")) {
            CompoundTag itemTag = compoundTag.getCompound("BackpackItem");
            stack = ItemStack.of(itemTag);
        }
        this.player.getEntityData().set(DATA_BACKPACK_STACK, stack);

        // 同步到背包
        ItemStack selected = BackpackSlotHelper.getSelectedBackpack(this.player);
        if (this.player.getInventory() instanceof IExtendedInventory extInv) {
            extInv.yyzsbackpack$syncFromBackpack(selected);
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
