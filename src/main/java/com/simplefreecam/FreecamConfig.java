package com.simplefreecam;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class FreecamConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("simplefreecam.txt");
    public static double speed = 0.8;

    public static void load() {
        try {
            if (Files.exists(FILE)) speed = Double.parseDouble(Files.readString(FILE).trim());
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, Double.toString(speed));
        } catch (Exception ignored) {
        }
    }
}
