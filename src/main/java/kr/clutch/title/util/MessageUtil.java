package kr.clutch.title.util;

import org.bukkit.configuration.file.FileConfiguration;

public final class MessageUtil {
    private MessageUtil() {
    }

    public static String message(FileConfiguration config, String path) {
        return config.getString("messages." + path, "");
    }

    public static String apply(String message, String placeholder, String value) {
        return message.replace("{" + placeholder + "}", value == null ? "" : value);
    }
}
