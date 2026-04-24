package com.yyz.yyzsbackpack.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;


public class BackpackKeyBinding {

    public static final KeyMapping KEY_SORT = new KeyMapping(
            "key.yyzsbackpack.sort",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.misc"
    );
}
