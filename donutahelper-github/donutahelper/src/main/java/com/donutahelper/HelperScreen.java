package com.donutahelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class HelperScreen extends Screen {
    private enum Tab {
        FLIP("Flip"), BLACKJACK("BJ"), PAPER("Paper"), TIMER("Timer"), SALES("Sales"), SETTINGS("Setup");
        final String label;
        Tab(String l) { label = l; }
    }

    private static Tab tab = Tab.FLIP;
    private static String timerMinutes = "60", salesItem = "", salesBuy = "", salesSell = "";
    private static boolean clearArmed = false;

    private ButtonWidget dealBtn, hitBtn, standBtn, dblBtn, splitBtn;
    private boolean dragging, wasDown;
    private int grabX, grabY;

    public HelperScreen() { super(Text.literal("Auction Helper")); }

    private int x0() { return width / 2 - 150 + HelperConfig.INSTANCE.guiOffX; }
    private int y0() { return height / 2 - 80 + HelperConfig.INSTANCE.guiOffY; }

    private TextFieldWidget field(int x, int y, int w, String label, String val, Consumer<String> listener) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 18, Text.literal(label));
        f.setText(val);
        f.setChangedListener(listener);
        addDrawableChild(f);
        return f;
    }

    private ButtonWidget button(String label, int x, int y, int w, Runnable action) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), btn -> action.run()).dimensions(x, y, w, 18).build();
        addDrawableChild(b);
        return b;
    }

    @Override
    protected void init() {
        HelperConfig cfg = HelperConfig.INSTANCE;
        int x = x0(), y = y0();
        dealBtn = hitBtn = standBtn = dblBtn = splitBtn = null;

        int tx = x;
        for (Tab t : Tab.values()) {
            ButtonWidget tb = ButtonWidget.builder(Text.literal(t.label), b -> { tab = t; clearArmed = false; clearAndInit(); })
                    .dimensions(tx, y - 26, 46, 20).build();
            tb.active = tab != t;
            addDrawableChild(tb);
            tx += 50;
        }

        switch (tab) {
            case FLIP -> {
                field(x + 90, y, 120, "Buy", cfg.lastBuy, s -> cfg.lastBuy = s);
                field(x + 90, y + 24, 120, "Sell", cfg.lastSell, s -> cfg.lastSell = s);
                button("Tax -1%", x + 90, y + 48, 58, () -> cfg.sellTaxPercent = Math.max(0, cfg.sellTaxPercent - 1));
                button("Tax +1%", x + 152, y + 48, 58, () -> cfg.sellTaxPercent = Math.min(100, cfg.sellTaxPercent + 1));
            }
            case BLACKJACK -> {
                BlackjackGame g = State.BLACKJACK;
                button("-1k", x, y, 44, () -> g.adjustBet(-1000));
                button("-100", x + 48, y, 44, () -> g.adjustBet(-100));
                button("+100", x + 96, y, 44, () -> g.adjustBet(100));
                button("+1k", x + 144, y, 44, () -> g.adjustBet(1000));
                dealBtn = button("Deal", x, y + 118, 54, g::deal);
                hitBtn = button("Hit", x + 58, y + 118, 54, g::hit);
                standBtn = button("Stand", x + 116, y + 118, 54, g::stand);
                dblBtn = button("Double", x + 174, y + 118, 54, g::doubleDown);
                splitBtn = button("Split", x + 232, y + 118, 54, g::split);
                button("Reset", x, y + 142, 54, g::resetBankroll);
            }
            case PAPER -> {
                for (int i = 0; i < 3; i++) {
                    final int choice = i;
                    button(PaperGame.NAMES[i], x + i * 70, y + 10, 64, () -> State.PAPER.play(choice));
                }
            }
            case TIMER -> {
                field(x + 90, y, 120, "Label", State.timerLabel, s -> State.timerLabel = s);
                field(x + 90, y + 24, 120, "Minutes", timerMinutes, s -> timerMinutes = s);
                button("Start", x + 90, y + 48, 58, () -> {
                    double m = Util.parse(timerMinutes);
                    if (!Double.isNaN(m) && m > 0) State.timerEnd = System.currentTimeMillis() + (long) (m * 60_000);
                });
                button("Clear", x + 152, y + 48, 58, () -> State.timerEnd = 0);
            }
            case SALES -> {
                field(x + 90, y, 120, "Item", salesItem, s -> salesItem = s);
                field(x + 90, y + 24, 120, "Buy", salesBuy, s -> salesBuy = s);
                field(x + 90, y + 48, 120, "Sell", salesSell, s -> salesSell = s);
                button("Log it", x + 90, y + 72, 58, () -> {
                    double b = Util.parse(salesBuy), s = Util.parse(salesSell);
                    if (Double.isNaN(b) || Double.isNaN(s)) return;
                    SalesLog.add(salesItem.isBlank() ? "item" : salesItem.trim(), b, s, cfg.sellTaxPercent);
                    salesBuy = ""; salesSell = "";
                    clearAndInit();
                });
                button("Undo last", x + 152, y + 72, 58, () -> SalesLog.undo());
                button(clearArmed ? "Sure?" : "Clear all", x + 214, y + 72, 58, () -> {
                    if (clearArmed) { SalesLog.clear(); clearArmed = false; } else clearArmed = true;
                    clearAndInit();
                });
            }
            case SETTINGS -> {
                int bw = 140;
                button("HUD: " + onOff(cfg.hudEnabled), x, y, bw, () -> { cfg.hudEnabled = !cfg.hudEnabled; clearAndInit(); });
                button("Flip line: " + onOff(cfg.showFlip), x + 150, y, bw, () -> { cfg.showFlip = !cfg.showFlip; clearAndInit(); });
                button("Timer line: " + onOff(cfg.showTimer), x, y + 22, bw, () -> { cfg.showTimer = !cfg.showTimer; clearAndInit(); });
                button("Blackjack line: " + onOff(cfg.showBlackjack), x + 150, y + 22, bw, () -> { cfg.showBlackjack = !cfg.showBlackjack; clearAndInit(); });
                button("Paper line: " + onOff(cfg.showPaper), x, y + 44, bw, () -> { cfg.showPaper = !cfg.showPaper; clearAndInit(); });
                button("Sales line: " + onOff(cfg.showSales), x + 150, y + 44, bw, () -> { cfg.showSales = !cfg.showSales; clearAndInit(); });
                button("Color: " + HelperConfig.COLOR_NAMES[cfg.colorIndex()], x, y + 66, bw, () -> {
                    cfg.hudColor = HelperConfig.COLORS[(cfg.colorIndex() + 1) % HelperConfig.COLORS.length]; clearAndInit();
                });
                button("BG opacity -", x + 150, y + 66, 68, () -> cfg.hudBgAlpha = Math.max(0, cfg.hudBgAlpha - 20));
                button("BG opacity +", x + 222, y + 66, 68, () -> cfg.hudBgAlpha = Math.min(255, cfg.hudBgAlpha + 20));
                button("Drag HUD...", x, y + 92, bw, () -> { HelperConfig.save(); client.setScreen(new HudEditScreen(this)); });
                button("Reset window pos", x + 150, y + 92, bw, () -> { cfg.guiOffX = 0; cfg.guiOffY = 0; HelperConfig.save(); clearAndInit(); });
            }
        }
    }

    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

    /** Drag the title bar above the tabs to move this window. */
    private void handleDrag(int mouseX, int mouseY) {
        HelperConfig cfg = HelperConfig.INSTANCE;
        long win = MinecraftClient.getInstance().getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int x = x0(), y = y0();

        if (down && !wasDown && mouseX >= x - 4 && mouseX <= x + 304 && mouseY >= y - 44 && mouseY <= y - 31) {
            dragging = true;
            grabX = mouseX - cfg.guiOffX;
            grabY = mouseY - cfg.guiOffY;
        }
        if (!down && dragging) { dragging = false; HelperConfig.save(); }
        if (dragging) {
            int maxX = Math.max(0, width / 2 - 150);
            int minY = -(height / 2 - 80 - 44);
            int maxY = Math.max(minY, height - 190 - (height / 2 - 80));
            int nx = Math.max(-maxX, Math.min(maxX, mouseX - grabX));
            int ny = Math.max(minY, Math.min(maxY, mouseY - grabY));
            if (nx != cfg.guiOffX || ny != cfg.guiOffY) {
                cfg.guiOffX = nx;
                cfg.guiOffY = ny;
                clearAndInit();
            }
        }
        wasDown = down;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        handleDrag(mouseX, mouseY);

        if (tab == Tab.BLACKJACK && dealBtn != null) {
            BlackjackGame g = State.BLACKJACK;
            boolean playing = g.phase == BlackjackGame.Phase.PLAYER;
            dealBtn.active = !playing;
            hitBtn.active = playing;
            standBtn.active = playing;
            dblBtn.active = g.canDouble();
            splitBtn.active = g.canSplit();
        }

        super.render(ctx, mouseX, mouseY, delta);

        HelperConfig cfg = HelperConfig.INSTANCE;
        int x = x0(), y = y0();
        int white = 0xFFFFFFFF, gray = 0xFFAAAAAA;

        ctx.fill(x - 4, y - 44, x + 304, y - 31, dragging ? 0xCC335533 : 0xCC000000);
        ctx.drawTextWithShadow(textRenderer, "Auction Helper  (drag here to move)", x, y - 42, gray);

        switch (tab) {
            case FLIP -> {
                ctx.drawTextWithShadow(textRenderer, "Buy price", x, y + 5, white);
                ctx.drawTextWithShadow(textRenderer, "Sell price", x, y + 29, white);
                ctx.drawTextWithShadow(textRenderer, "Sell tax: " + cfg.sellTaxPercent + "%", x, y + 53, white);
                double b = Util.parse(cfg.lastBuy), s = Util.parse(cfg.lastSell);
                if (Double.isNaN(b) || Double.isNaN(s)) {
                    ctx.drawTextWithShadow(textRenderer, "Enter prices like 1.5k, 2m, 3b", x, y + 80, gray);
                } else {
                    double net = s * (1 - cfg.sellTaxPercent / 100.0);
                    double profit = net - b;
                    double roi = b > 0 ? profit / b * 100 : 0;
                    int col = profit >= 0 ? 0xFF55FF55 : 0xFFFF5555;
                    ctx.drawTextWithShadow(textRenderer, "Net after tax: " + Util.fmt(net), x, y + 80, white);
                    ctx.drawTextWithShadow(textRenderer, "Profit: " + Util.fmt(profit), x, y + 92, col);
                    ctx.drawTextWithShadow(textRenderer, String.format("ROI: %.1f%%", roi), x, y + 104, col);
                    ctx.drawTextWithShadow(textRenderer, "Break-even sell: " + Util.fmt(b / (1 - cfg.sellTaxPercent / 100.0)), x, y + 116, gray);
                }
            }
            case BLACKJACK -> {
                BlackjackGame g = State.BLACKJACK;
                boolean playing = g.phase == BlackjackGame.Phase.PLAYER;
                ctx.drawTextWithShadow(textRenderer, "Play money  Bankroll: " + Util.fmt(g.bankroll) + "   Bet: " + Util.fmt(g.bet), x, y + 24, white);
                ctx.drawTextWithShadow(textRenderer, "Dealer: " + g.hand(g.dealer, playing)
                        + (playing || g.dealer.isEmpty() ? "" : " (" + BlackjackGame.total(g.dealer) + ")"), x, y + 40, white);
                for (int i = 0; i < g.hands.size(); i++) {
                    BlackjackGame.Hand h = g.hands.get(i);
                    String line = (playing && i == g.active ? "> " : "  ") + "Hand " + (i + 1) + ": " + g.hand(h.cards, false)
                            + " (" + BlackjackGame.total(h.cards) + ")  bet " + Util.fmt(h.bet) + (h.doubled ? " x2" : "");
                    ctx.drawTextWithShadow(textRenderer, line, x, y + 54 + i * 12, playing && i == g.active ? 0xFFFFFF55 : white);
                }
                ctx.drawTextWithShadow(textRenderer, g.message, x, y + 106, 0xFFFFAA00);
                ctx.drawTextWithShadow(textRenderer, "W " + g.wins + "  L " + g.losses + "  P " + g.pushes, x + 64, y + 147, gray);
            }
            case PAPER -> {
                PaperGame p = State.PAPER;
                ctx.drawTextWithShadow(textRenderer, p.message, x, y + 44, 0xFFFFAA00);
                ctx.drawTextWithShadow(textRenderer, "W " + p.wins + "  L " + p.losses + "  T " + p.ties, x, y + 62, white);
                ctx.drawTextWithShadow(textRenderer, "Streak " + p.streak + "  Best " + p.bestStreak, x, y + 74, gray);
            }
            case TIMER -> {
                ctx.drawTextWithShadow(textRenderer, "Label", x, y + 5, white);
                ctx.drawTextWithShadow(textRenderer, "Minutes", x, y + 29, white);
                long left = State.timerEnd - System.currentTimeMillis();
                String t = left > 0 ? Util.time(left) : "No timer running";
                ctx.drawTextWithShadow(textRenderer, (State.timerLabel.isEmpty() ? "" : State.timerLabel + ": ") + t, x, y + 80, 0xFFFFAA00);
            }
            case SALES -> {
                ctx.drawTextWithShadow(textRenderer, "Item", x, y + 5, white);
                ctx.drawTextWithShadow(textRenderer, "Buy price", x, y + 29, white);
                ctx.drawTextWithShadow(textRenderer, "Sell price", x, y + 53, white);
                double total = SalesLog.total();
                ctx.drawTextWithShadow(textRenderer, "Total profit: " + (total >= 0 ? "+" : "") + Util.fmt(total)
                        + "  (" + SalesLog.entries.size() + " sales, tax " + cfg.sellTaxPercent + "%)", x, y + 98, total >= 0 ? 0xFF55FF55 : 0xFFFF5555);
                List<SalesLog.Entry> es = SalesLog.entries;
                for (int i = 0; i < Math.min(5, es.size()); i++) {
                    SalesLog.Entry e = es.get(es.size() - 1 - i);
                    ctx.drawTextWithShadow(textRenderer, e.item + ": " + (e.profit >= 0 ? "+" : "") + Util.fmt(e.profit)
                            + "  (" + Util.fmt(e.buy) + " -> " + Util.fmt(e.sell) + ")", x, y + 112 + i * 11, e.profit >= 0 ? white : 0xFFFF8888);
                }
                ctx.drawTextWithShadow(textRenderer, "Entries are typed in by you. Tax is set on the Flip tab.", x, y + 170, gray);
            }
            case SETTINGS -> ctx.drawTextWithShadow(textRenderer, "HUD look and position. Saved automatically.", x, y + 122, gray);
        }
    }

    @Override
    public void close() {
        HelperConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
