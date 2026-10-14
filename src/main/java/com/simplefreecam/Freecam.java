package com.simplefreecam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Camera-only freecam. Purely visual: it never sends any extra packets and never touches
 * the server. Your real character just stands still while the camera flies around.
 */
public final class Freecam {
    private static boolean active = false;
    private static ArmorStandEntity cam;
    private static Input savedInput;
    private static boolean savedCulling = true;
    private static double vx, vy, vz;

    private Freecam() {}

    public static boolean isActive() {
        return active;
    }

    public static Entity camera() {
        return cam;
    }

    public static void toggle() {
        if (active) disable("Freecam: OFF");
        else enable();
    }

    private static void enable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        cam = new ArmorStandEntity(mc.world, p.getX(), p.getY(), p.getZ());
        place(p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch());
        vx = vy = vz = 0;

        // Freeze the real player: neutral input, so it stands still.
        savedInput = p.input;
        p.input = new Input() {
            @Override
            public void tick() {
            }
        };
        savedCulling = mc.chunkCullingEnabled;
        mc.chunkCullingEnabled = false;
        mc.setCameraEntity(cam);
        active = true;
        p.sendMessage(Text.literal("Freecam: ON"), true);
    }

    public static void disable(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!active) return;
        active = false;
        ClientPlayerEntity p = mc.player;
        if (p != null) {
            if (savedInput != null) p.input = savedInput;
            mc.setCameraEntity(p);
            if (msg != null) p.sendMessage(Text.literal(msg), true);
        } else {
            mc.setCameraEntity(null);
        }
        mc.chunkCullingEnabled = savedCulling;
        savedInput = null;
        cam = null;
    }

    /** Called at the start of every client tick. */
    public static void tick(MinecraftClient mc) {
        if (!active) return;
        ClientPlayerEntity p = mc.player;
        // Safety switch: leave freecam instantly on anything dangerous.
        if (p == null || mc.world == null || !p.isAlive() || p.hurtTime > 0 || cam == null) {
            disable(p != null && p.hurtTime > 0 ? "Freecam: OFF (you took damage)" : "Freecam: OFF");
            return;
        }
        if (mc.getCameraEntity() != cam) mc.setCameraEntity(cam);
        mc.chunkCullingEnabled = false;

        GameOptions o = mc.options;
        // No attacking, placing, picking, dropping or swapping while the camera is out.
        for (var k : new net.minecraft.client.option.KeyBinding[]{o.attackKey, o.useKey, o.pickItemKey, o.dropKey, o.swapHandsKey}) {
            while (k.wasPressed()) { /* swallow */ }
            k.setPressed(false);
        }

        double speed = FreecamConfig.speed;
        if (speed < 0.05) speed = 0.05;
        if (o.sprintKey.isPressed()) speed *= 3;

        double f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        double s = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
        double u = (o.jumpKey.isPressed() ? 1 : 0) - (o.sneakKey.isPressed() ? 1 : 0);

        double yaw = Math.toRadians(cam.getYaw());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        double tx = (-sin * f + cos * s) * speed;
        double tz = (cos * f + sin * s) * speed;
        double ty = u * speed;

        vx += (tx - vx) * 0.5;
        vy += (ty - vy) * 0.5;
        vz += (tz - vz) * 0.5;

        place(cam.getX() + vx, cam.getY() + vy, cam.getZ() + vz, cam.getYaw(), cam.getPitch());
    }

    /** Mouse look, called from the mixin. */
    public static void look(double dx, double dy) {
        if (cam == null) return;
        float yaw = cam.getYaw() + (float) dx * 0.15f;
        float pitch = MathHelper.clamp(cam.getPitch() + (float) dy * 0.15f, -90f, 90f);
        cam.setYaw(yaw);
        cam.setPitch(pitch);
        cam.lastYaw = yaw;
        cam.lastPitch = pitch;
    }

    private static void place(double x, double y, double z, float yaw, float pitch) {
        cam.lastX = cam.lastRenderX = cam.getX();
        cam.lastY = cam.lastRenderY = cam.getY();
        cam.lastZ = cam.lastRenderZ = cam.getZ();
        cam.setPosition(x, y, z);
        cam.setYaw(yaw);
        cam.setPitch(pitch);
        cam.lastYaw = yaw;
        cam.lastPitch = pitch;
    }
}
