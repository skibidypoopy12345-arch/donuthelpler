package com.donutahelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HudLines {
    public static List<String> build(boolean sampleIfEmpty) {
        HelperConfig cfg = HelperConfig.INSTANCE;
        List<String> lines = new ArrayList<>();

        if (cfg.showFlip) {
            double b = Util.parse(cfg.lastBuy), s = Util.parse(cfg.lastSell);
            if (!Double.isNaN(b) && !Double.isNaN(s)) {
                double profit = s * (1 - cfg.sellTaxPercent / 100.0) - b;
                lines.add("Flip: " + (profit >= 0 ? "+" : "") + Util.fmt(profit));
            }
        }
        if (cfg.showTimer && State.timerEnd > System.currentTimeMillis()) {
            lines.add("Timer: " + Util.time(State.timerEnd - System.currentTimeMillis()));
        }
        if (cfg.showSales && !SalesLog.entries.isEmpty()) {
            double t = SalesLog.total();
            lines.add("Sales: " + (t >= 0 ? "+" : "") + Util.fmt(t) + " (" + SalesLog.entries.size() + ")");
        }
        if (cfg.showBlackjack) {
            BlackjackGame bj = State.BLACKJACK;
            lines.add("BJ: " + Util.fmt(bj.bankroll) + " (" + bj.wins + "W/" + bj.losses + "L)");
        }
        if (cfg.showPaper) {
            PaperGame pg = State.PAPER;
            lines.add("RPS: " + pg.wins + "W/" + pg.losses + "L streak " + pg.streak);
        }
        if (lines.isEmpty() && sampleIfEmpty) lines.add("HUD preview");
        return lines;
    }

    public static int width(List<String> lines) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = 0;
        for (String l : lines) w = Math.max(w, mc.textRenderer.getWidth(l));
        return w + 4;
    }

    public static int height(List<String> lines) { return lines.size() * 10 + 2; }

    public static void draw(DrawContext ctx, List<String> lines) {
        HelperConfig cfg = HelperConfig.INSTANCE;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (lines.isEmpty()) return;
        int w = width(lines), h = height(lines);
        ctx.fill(cfg.hudX - 2, cfg.hudY - 2, cfg.hudX - 2 + w, cfg.hudY - 2 + h, (cfg.hudBgAlpha << 24));
        int y = cfg.hudY;
        for (String l : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, l, cfg.hudX, y, cfg.hudColor);
            y += 10;
        }
    }
}
