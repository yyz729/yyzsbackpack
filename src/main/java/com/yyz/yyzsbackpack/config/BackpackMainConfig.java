package com.yyz.yyzsbackpack.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yyz.yyzsbackpack.api.enums.ButtonMode;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BackpackMainConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 全局读配置都从这里拿，reload 后自动变新。 */
    private static volatile BackpackMainConfig INSTANCE;
    /** 记住加载时用的文件，reload 时不用再问外面。 */
    private static volatile File configFile;

    public boolean model = true;
    public int heavy = 2;
    public ButtonMode button = ButtonMode.SHOW;

    /** 拿最新实例；如果还没加载过就先加载一次。 */
    public static BackpackMainConfig getInstance() {
        if (INSTANCE == null) {
            File f = (configFile != null)
                    ? configFile
                    : FMLPaths.CONFIGDIR.get()
                    .resolve("yyzsbackpack/yyzsbackpack.json").toFile();
            loadConfig(f);
        }
        return INSTANCE;
    }

    public static BackpackMainConfig loadConfig(File file) {
        BackpackMainConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                config = GSON.fromJson(bufferedReader, BackpackMainConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new BackpackMainConfig();
        }

        if (config == null) config = new BackpackMainConfig();

        config.saveConfig(file);

        INSTANCE = config;
        configFile = file;
        return config;
    }

    /** 从上次加载用的文件重新读一遍，替换 INSTANCE。 */
    public static BackpackMainConfig reload() {
        File f = configFile;
        if (f == null) {
            f = FMLPaths.CONFIGDIR.get()
                    .resolve("yyzsbackpack/yyzsbackpack.json").toFile();
        }
        return loadConfig(f);
    }

    public void saveConfig(File config) {
        Path configPath = config.toPath();
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory: " + config.getParent(), e);
        }

        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }
}