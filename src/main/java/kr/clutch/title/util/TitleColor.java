package kr.clutch.title.util;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class TitleColor {
    private static final Map<String, String> NAME_TO_CODE = Map.ofEntries(
            Map.entry("black", "§0"),
            Map.entry("dark_blue", "§1"),
            Map.entry("dark_green", "§2"),
            Map.entry("dark_aqua", "§3"),
            Map.entry("dark_red", "§4"),
            Map.entry("dark_purple", "§5"),
            Map.entry("gold", "§6"),
            Map.entry("gray", "§7"),
            Map.entry("dark_gray", "§8"),
            Map.entry("blue", "§9"),
            Map.entry("green", "§a"),
            Map.entry("aqua", "§b"),
            Map.entry("red", "§c"),
            Map.entry("light_purple", "§d"),
            Map.entry("yellow", "§e"),
            Map.entry("white", "§f")
    );

    private TitleColor() {
    }

    public static String defaultColorCode() {
        return "§f";
    }

    public static Optional<String> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.of(defaultColorCode());
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        String namedColor = NAME_TO_CODE.get(normalized);
        if (namedColor != null) {
            return Optional.of(namedColor);
        }

        if (normalized.length() == 2 && (normalized.charAt(0) == '§' || normalized.charAt(0) == '&')) {
            char code = normalized.charAt(1);
            if (isSupportedLegacyColorCode(code)) {
                return Optional.of("§" + code);
            }
        }

        return Optional.empty();
    }

    public static String display(String titleName, String colorCode) {
        return colorCode + "[" + titleName + "]";
    }

    private static boolean isSupportedLegacyColorCode(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }
}
