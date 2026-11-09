package com.donutahelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class HelperClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        HelperConfig.load();
        SalesLog.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("donutahelper", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.donutahelper.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) client.setScreen(new HelperScreen());
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                Identifier.of("donutahelper", "hud"), (context, tickCounter) -> {
                    if (!HelperConfig.INSTANCE.hudEnabled) return;
                    HudLines.draw(context, HudLines.build(false));
                });
    }
}
