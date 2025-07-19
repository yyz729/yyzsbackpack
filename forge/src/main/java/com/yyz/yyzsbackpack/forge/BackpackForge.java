package com.yyz.yyzsbackpack.forge;

import com.yyz.yyzsbackpack.Backpack;

import com.yyz.yyzsbackpack.BackpackHelper;
import com.yyz.yyzsbackpack.forge.compat.curios.CuriosContainerAdapter;
import com.yyz.yyzsbackpack.item.BackpackItem;
import com.yyz.yyzsbackpack.item.BackpackMaterial;
import com.yyz.yyzsbackpack.compat.AccessoriesContainerAdapter;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.CuriosConfig;

import java.util.Map;
import java.util.Optional;

@Mod(Backpack.MOD_ID)
public final class BackpackForge {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,Backpack.MOD_ID);
    public static final RegistryObject<CreativeModeTab> BACKPACK_TAB = TABS.register("backpack_tab", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(BackpackForge.GOLD_BACKPACK.get()))
                    .title(Component.translatable("itemGroup.yyzsbackpack.title"))
                    .displayItems((context, entries) -> {
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
    public static final RegistryObject<Item> WOOLEN_BACKPACK = ITEMS.register("woolen_backpack", () -> new BackpackItem(BackpackMaterial.WOODEN, new Item.Properties().stacksTo(1)));


    public static ItemStack getEquipped(Player player) {
        if(!Backpack.getConfig().force_slot) {
            // 1. 优先检查 Curios
            if (BackpackHelper.isModLoaded("curios")) {
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

            // 2. 检查 Accessories
            else if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios")) {
                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    Map<String, AccessoriesContainer> containers = capability.getContainers();
                    for (AccessoriesContainer container : containers.values()) {
                        Container accessoriesContainer = container.getAccessories();
                        for (int i = 0; i < accessoriesContainer.getContainerSize(); i++) {
                            ItemStack stack = accessoriesContainer.getItem(i);
                            if (stack.getItem() instanceof BackpackItem) {
                                return stack;
                            }
                        }
                    }
                }
            }
        }
        // 3. 默认返回
        return player.getInventory().getItem(36+54);
    }

    public static Container getContainer(Player player) {
        if(!Backpack.getConfig().force_slot) {
            // 1. 优先检查 Curios
            if (BackpackHelper.isModLoaded("curios")) {
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

            // 2. 检查 Accessories
            else if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios")) {
                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    Map<String, AccessoriesContainer> containers = capability.getContainers();
                    for (AccessoriesContainer container : containers.values()) {
                        Container accessoriesContainer = container.getAccessories();
                        for (int i = 0; i < accessoriesContainer.getContainerSize(); i++) {
                            ItemStack stack = accessoriesContainer.getItem(i);
                            if (stack.getItem() instanceof BackpackItem) {
                                return new AccessoriesContainerAdapter(accessoriesContainer, i);
                            }
                        }
                    }
                }
            }
        }
        // 3. 默认返回玩家物品栏
        return player.getInventory();
    }

    public static int getIndex(Player player) {
        if(!Backpack.getConfig().force_slot) {
            // 1. 优先检查 Curios
            if (BackpackHelper.isModLoaded("curios")) {
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

            // 2. 检查 Accessories
            else if (BackpackHelper.isModLoaded("accessories") && !BackpackHelper.isModLoaded("curios") ) {
                Container container = getContainer(player);
                if (container instanceof AccessoriesContainerAdapter) {
                    return ((AccessoriesContainerAdapter) container).getBackpackSlotIndex();
                }
            }
        }
        // 3. 默认返回索引
        return 36+54;
    }
    public BackpackForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TABS.register(modEventBus);
        ITEMS.register(modEventBus);
        Backpack.init();


    }


    public static boolean getEmptyRule(Player player) {
        if(BackpackHelper.isModLoaded("curios")){
            if(!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && CuriosConfig.SERVER.keepCurios.get() == CuriosConfig.KeepCurios.ON){
                return false;
            }
        }
        return true;
    }
}