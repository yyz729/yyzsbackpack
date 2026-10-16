package com.yyz.yyzsbackpack.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import com.yyz.yyzsbackpack.client.gui.screen.BackpackConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;


public class BackpackKeyBinding {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "category")
    );

    public static final KeyMapping KEY_SORT;
    public static final KeyMapping KEY_OPEN;
    public static final KeyMapping KEY_CONFIG;

    static {
        KEY_SORT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yyzsbackpack.sort",
                InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        KEY_OPEN = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yyzsbackpack.open",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_B,
                CATEGORY
        ));

        KEY_CONFIG = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yyzsbackpack.config",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RBRACKET,
                CATEGORY
        ));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KEY_CONFIG.consumeClick()) {
                if (client.gui.screen() == null) {
                    BackpackConfigScreen.open(null);
                }
            }
        });
    }
}