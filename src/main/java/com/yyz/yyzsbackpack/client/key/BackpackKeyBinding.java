package com.yyz.yyzsbackpack.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class BackpackKeyBinding {

    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "category"));

    public static final KeyMapping KEY_SORT;
    public static final KeyMapping KEY_OPEN;

    static {
        KEY_SORT = new KeyMapping(
                "key.yyzsbackpack.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        );

        KEY_OPEN = new KeyMapping(
                "key.yyzsbackpack.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        );
    }
}