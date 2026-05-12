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

    public static String defaultColor() {
        return "§f";
    }

    public static Optional<String> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.of(defaultColor());
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

        if (normalized.matches("#[0-9a-f]{6}")) {
            return Optional.of(toLegacyHex(normalized));
        }

        return Optional.empty();
    }

    public static String displayName(String titleName, String color) {
        return color + "[" + titleName + "]";
    }

    public static boolean isHexColor(String color) {
        return color != null && color.matches("§x(§[0-9a-fA-F]){6}");
    }

    private static String toLegacyHex(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (int index = 1; index < hex.length(); index++) {
            builder.append('§').append(Character.toUpperCase(hex.charAt(index)));
        }
        return builder.toString();
    }

    private static boolean isSupportedLegacyColorCode(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }
}
