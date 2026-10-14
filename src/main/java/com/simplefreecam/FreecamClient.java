package com.simplefreecam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FreecamClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FreecamConfig.load();
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("simplefreecam", "main"));
        KeyBinding toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.simplefreecam.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, cat));
        KeyBinding faster = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.simplefreecam.faster", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, cat));
        KeyBinding slower = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.simplefreecam.slower", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, cat));

        ClientTickEvents.START_CLIENT_TICK.register(Freecam::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> Freecam.disable(null));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggle.wasPressed()) Freecam.toggle();
            boolean changed = false;
            while (faster.wasPressed()) { FreecamConfig.speed = Math.min(3.0, FreecamConfig.speed + 0.2); changed = true; }
            while (slower.wasPressed()) { FreecamConfig.speed = Math.max(0.1, FreecamConfig.speed - 0.2); changed = true; }
            if (changed) {
                FreecamConfig.save();
                if (client.player != null)
                    client.player.sendMessage(Text.literal(String.format("Freecam speed: %.1f", FreecamConfig.speed)), true);
            }
        });
    }
}
