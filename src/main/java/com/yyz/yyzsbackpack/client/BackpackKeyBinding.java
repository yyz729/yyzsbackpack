package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BackpackKeyBinding {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "category")
    );

    public static final KeyMapping KEY_SORT = new KeyMapping(
            "key.yyzsbackpack.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );

    public static final KeyMapping KEY_OPEN = new KeyMapping(
            "key.yyzsbackpack.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );

    public static List<KeyMapping> getAllBindings() {
        return List.of(KEY_SORT, KEY_OPEN);
    }
}