package com.donutahelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Manual log of your own flips/sales. You enter the numbers; nothing is read from the server. */
public class SalesLog {
    public static class Entry {
        public String item;
        public double buy, sell, profit;
        public long time;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("donutahelper_sales.json");
    public static List<Entry> entries = new ArrayList<>();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                List<Entry> l = GSON.fromJson(Files.readString(FILE), new TypeToken<List<Entry>>() {}.getType());
                if (l != null) entries = l;
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(entries)); } catch (Exception ignored) {}
    }

    public static void add(String item, double buy, double sell, double taxPercent) {
        Entry e = new Entry();
        e.item = item; e.buy = buy; e.sell = sell;
        e.profit = sell * (1 - taxPercent / 100.0) - buy;
        e.time = System.currentTimeMillis();
        entries.add(e);
        save();
    }

    public static void undo() {
        if (!entries.isEmpty()) entries.remove(entries.size() - 1);
        save();
    }

    public static void clear() { entries.clear(); save(); }

    public static double total() {
        double t = 0;
        for (Entry e : entries) t += e.profit;
        return t;
    }
}
