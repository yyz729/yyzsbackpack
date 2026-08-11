package com.yyz.yyzsbackpack.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.yyz.yyzsbackpack.Backpack;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BackpackKeyBinding {


    public static final KeyMapping KEY_SORT;
    public static final KeyMapping KEY_OPEN;

    static {
        KEY_SORT = new KeyMapping(
                "key.yyzsbackpack.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category."+Backpack.MOD_ID + ".category"
        );

        KEY_OPEN = new KeyMapping(
                "key.yyzsbackpack.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.category."+Backpack.MOD_ID + ".category"
        );
    }
}