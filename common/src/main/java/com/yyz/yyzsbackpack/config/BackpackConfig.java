package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class BackpackConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean quick_swap = true;//是否启用快速切换背包
    public boolean force_slot = false;//是否强制使用自带的背包装备槽而不使用饰品栏
    public boolean render_model = true;//是是否渲染背包模型
    public boolean container_item = false;//是否禁止container物品（比如潜影盒）放入背包
    public int slot_offsetX = 0;//自带的背包装备槽的坐标
    public int slot_offsetY = 0;//自带的背包装备槽的坐标
    public int backpack_offsetX = 0;//背包内部空间的坐标
    public int backpack_offsetY = 0;//背包内部空间的坐标
    public String tip_key = "shift";//修饰键alt,ctrl,none
    public Set<String> container_item_list = new HashSet<>(Set.of(//额外的container物品
            "namespace:item_id"
    ));


    public static BackpackConfig loadConfig(File file) {
        BackpackConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                config = GSON.fromJson(bufferedReader, BackpackConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new BackpackConfig();
        }

        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File config) {
        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }



}
