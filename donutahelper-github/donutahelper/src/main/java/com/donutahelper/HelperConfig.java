package com.donutahelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class HelperConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("donutahelper.json");
    public static HelperConfig INSTANCE = new HelperConfig();

    public boolean hudEnabled = true;
    public int hudX = 6;
    public int hudY = 6;
    public int hudColor = 0xFFFFFFFF;
    public int hudBgAlpha = 120;      // 0-255
    public boolean showFlip = true;
    public boolean showTimer = true;
    public boolean showBlackjack = true;
    public boolean showPaper = true;
    public boolean showSales = true;
    public int guiOffX = 0;           // main window offset from center
    public int guiOffY = 0;
    public double sellTaxPercent = 0;
    public String lastBuy = "";
    public String lastSell = "";

    public static final int[] COLORS = {0xFFFFFFFF, 0xFF55FF55, 0xFFFFAA00, 0xFF55FFFF, 0xFFFF5555, 0xFFFF55FF};
    public static final String[] COLOR_NAMES = {"White", "Green", "Gold", "Aqua", "Red", "Pink"};

    public int colorIndex() {
        for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == hudColor) return i;
        return 0;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) INSTANCE = GSON.fromJson(Files.readString(FILE), HelperConfig.class);
            if (INSTANCE == null) INSTANCE = new HelperConfig();
        } catch (Exception e) { INSTANCE = new HelperConfig(); }
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(INSTANCE)); } catch (Exception ignored) {}
    }
}
