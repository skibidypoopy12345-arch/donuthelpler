package com.donutahelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Drag the HUD box with the left mouse button, then press Done. */
public class HudEditScreen extends Screen {
    private final Screen parent;
    private boolean dragging, wasDown;
    private int grabX, grabY;

    public HudEditScreen(Screen parent) {
        super(Text.literal("Move HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 40, height - 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        HelperConfig cfg = HelperConfig.INSTANCE;
        List<String> lines = HudLines.build(true);
        int w = HudLines.width(lines), h = HudLines.height(lines);

        long win = MinecraftClient.getInstance().getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int bx = cfg.hudX - 2, by = cfg.hudY - 2;
        if (down && !wasDown && mouseX >= bx && mouseX <= bx + w && mouseY >= by && mouseY <= by + h) {
            dragging = true;
            grabX = mouseX - cfg.hudX;
            grabY = mouseY - cfg.hudY;
        }
        if (!down) dragging = false;
        if (dragging) {
            cfg.hudX = Math.max(2, Math.min(width - w, mouseX - grabX));
            cfg.hudY = Math.max(2, Math.min(height - h, mouseY - grabY));
        }
        wasDown = down;

        HudLines.draw(ctx, lines);
        ctx.drawBorder(cfg.hudX - 2, cfg.hudY - 2, w, h, dragging ? 0xFF55FF55 : 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Drag the box to move your HUD", width / 2, height / 2, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        HelperConfig.save();
        if (client != null) client.setScreen(parent);
    }
}
