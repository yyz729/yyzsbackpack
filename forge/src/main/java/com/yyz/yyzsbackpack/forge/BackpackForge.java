package com.yyz.yyzsbackpack.forge;

import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;

import mezz.jei.gui.startup.JeiEventHandlers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
//import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
//import org.violetmoon.quark.base.Quark;

@Mod(Backpack.MOD_ID)
public final class BackpackForge {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,Backpack.MOD_ID);
    public static final RegistryObject<CreativeModeTab> BACKPACK_TAB = TABS.register("backpack_tab", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(BackpackForge.GOLD_BACKPACK.get()))
                    .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                    .displayItems((context, entries) -> {
                        // 调整顺序：木头背包在最前面，从低级到高级
                        entries.accept(BackpackForge.WOOLEN_BACKPACK.get());
                        entries.accept(BackpackForge.STONE_BACKPACK.get());
                        entries.accept(BackpackForge.IRON_BACKPACK.get());
                        entries.accept(BackpackForge.GOLD_BACKPACK.get());
                        entries.accept(BackpackForge.DIAMOND_BACKPACK.get());
                        entries.accept(BackpackForge.NETHERITE_BACKPACK.get());
                    })
                    .build());

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,Backpack.MOD_ID);

    public static final RegistryObject<Item> NETHERITE_BACKPACK = ITEMS.register("netherite_backpack", () -> new BackpackItem(BackpackMaterial.NETHERITE, new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> DIAMOND_BACKPACK = ITEMS.register("diamond_backpack", () -> new BackpackItem(BackpackMaterial.DIAMOND, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GOLD_BACKPACK = ITEMS.register("gold_backpack", () -> new BackpackItem(BackpackMaterial.GOLD, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IRON_BACKPACK = ITEMS.register("iron_backpack", () -> new BackpackItem(BackpackMaterial.IRON, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STONE_BACKPACK = ITEMS.register("stone_backpack", () -> new BackpackItem(BackpackMaterial.STONE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WOOLEN_BACKPACK = ITEMS.register("woolen_backpack", () -> new BackpackItem(BackpackMaterial.WOOLEN, new Item.Properties().stacksTo(1)));


//    public static ItemStack getEquipped(Player player) {
//        return player.getInventory().getItem(36);
//    }

//    public static Container getContainer(Player player) {
//        return player.getInventory();
//    }

    public static ItemStack getEquipped(Player player) {
        if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
            Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player).resolve();
            if (curiosHandler.isPresent()) {
                Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                        stack -> stack.getItem() instanceof BackpackItem
                );
                if (backpackSlot.isPresent()) {
                    return backpackSlot.get().stack();
                }
            }
        }
        return player.getInventory().getItem(36);
    }

public static Container getContainer(Player player) {
    if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player).resolve();
        if (curiosHandler.isPresent()) {
            Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                    stack -> stack.getItem() instanceof BackpackItem
            );
            if (backpackSlot.isPresent()) {
                String slotId = backpackSlot.get().slotContext().identifier();
                int slotIndex = backpackSlot.get().slotContext().index();

                Optional<ICurioStacksHandler> stacksHandler = curiosHandler.get().getStacksHandler(slotId);
                if (stacksHandler.isPresent()) {
                    return new CuriosContainerAdapter(stacksHandler.get().getStacks(), slotIndex);
                }
            }
        }

    }
    return player.getInventory();
}
//    public static int getIndex(Player player) {
//       return 36;
//    }

    public static int getIndex(Player player) {
        if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
            Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player).resolve();
            if (curiosHandler.isPresent()) {
                Optional<SlotResult> backpackSlot = curiosHandler.get().findFirstCurio(
                        stack -> stack.getItem() instanceof BackpackItem
                );
                if (backpackSlot.isPresent()) {
                    CuriosContainerAdapter container = (CuriosContainerAdapter) getContainer(player);
                    if (container != null) {
                        return container.getBackpackSlotIndex();
                    }
                }
            }
        }
        return 36;
    }
    public BackpackForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TABS.register(modEventBus);
        ITEMS.register(modEventBus);
        Backpack.init();
    }


}