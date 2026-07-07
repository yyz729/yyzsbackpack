//package com.yyz.yyzsbackpack.mixin.minecraft;
//
//import com.yyz.yyzsbackpack.api.IBackpackMenu;
//import net.minecraft.world.inventory.ChestMenu;
//import net.minecraft.world.inventory.CrafterMenu;
//import net.minecraft.world.inventory.InventoryMenu;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//
//@Mixin({InventoryMenu.class, ChestMenu.class, CrafterMenu.class})
//public class BackpackMenuMixin implements IBackpackMenu
//{
//    @Unique
//    private int backpackSlotStart;
//
//    @Override
//    public int yyzsbackpack$getBackpackSlotStart() {
//        return backpackSlotStart;
//    }
//
//    @Override
//    public void yyzsbackpack$setBackpackSlotStart(int start) {
//        this.backpackSlotStart = start;
//    }
//}
