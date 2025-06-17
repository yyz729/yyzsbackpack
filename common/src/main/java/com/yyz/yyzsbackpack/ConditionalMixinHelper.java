package com.yyz.yyzsbackpack;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ConditionalMixinHelper {
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }
}
