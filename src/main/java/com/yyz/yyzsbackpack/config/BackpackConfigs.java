package com.yyz.yyzsbackpack.config;

import java.util.List;
import java.util.Map;

public class BackpackConfigs {
    public static BackpackMainConfig main() {
        return BackpackMainConfig.getInstance();
    }

    public static Map<String, List<int[]>> control() {
        return BackpackControlConfig.getInstance().getControlPoss();
    }
    public static Map<String, List<int[]>> offset() {
        return BackpackOffsetConfig.getInstance().getOffsetValues();
    }
    public static Map<String, List<int[]>> ui() {
        return BackpackUiConfig.getInstance().getUiOffsets();
    }
}