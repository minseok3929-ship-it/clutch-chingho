package kr.clutch.title.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public final class MessageUtil {
    private static final String PREFIX = "§8[CLUTCH] ";

    private MessageUtil() {
    }

    public static String message(FileConfiguration config, String path) {
        return format(config.getString("messages." + path, ""), Map.of());
    }

    public static String message(FileConfiguration config, String path, Map<String, String> placeholders) {
        return format(config.getString("messages." + path, ""), placeholders);
    }

    public static void send(CommandSender sender, FileConfiguration config, String path) {
        sender.sendMessage(message(config, path));
    }

    public static void send(CommandSender sender, FileConfiguration config, String path, Map<String, String> placeholders) {
        sender.sendMessage(message(config, path, placeholders));
    }

    public static void sendRaw(CommandSender sender, String rawMessage) {
        sender.sendMessage(format(rawMessage, Map.of()));
    }

    public static void sendRaw(CommandSender sender, String rawMessage, Map<String, String> placeholders) {
        sender.sendMessage(format(rawMessage, placeholders));
    }

    public static String format(String message, Map<String, String> placeholders) {
        String formatted = message == null ? "" : message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            formatted = formatted
                    .replace("<" + entry.getKey() + ">", value)
                    .replace("{" + entry.getKey() + "}", value);
        }
        formatted = ChatColor.translateAlternateColorCodes('&', formatted);
        if (formatted.isBlank()) {
            return PREFIX + "§c메시지가 설정되지 않았습니다.";
        }
        if (formatted.startsWith(PREFIX)) {
            return formatted;
        }
        return PREFIX + formatted;
    }

    public static String apply(String message, String placeholder, String value) {
        return format(message, Map.of(placeholder, value == null ? "" : value));
    }
}
