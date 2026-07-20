package com.yyz.yyzsbackpack.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BackpackKeyBinding {


    public static final KeyMapping KEY_SORT = new KeyMapping(
            "key.yyzsbackpack.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            Backpack.MOD_ID + ".category"
    );

    public static final KeyMapping KEY_OPEN = new KeyMapping(
            "key.yyzsbackpack.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            Backpack.MOD_ID + ".category"
    );

    public static List<KeyMapping> getAllBindings() {
        return List.of(KEY_SORT, KEY_OPEN);
    }
}