package com.yyz.yyzsbackpack.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BackpackKeyBinding {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "category")
    );

    public static final KeyMapping KEY_SORT;
    public static final KeyMapping KEY_OPEN;

    static {
        KEY_SORT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yyzsbackpack.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

        KEY_OPEN = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yyzsbackpack.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));
    }

    public static void register() {

    }
}