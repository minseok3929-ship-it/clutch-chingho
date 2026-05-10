package kr.clutch.title.listener;

import kr.clutch.title.service.TitleDisplayService;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;

public final class ChatListener implements Listener {
    private final FileConfiguration config;
    private final TitleDisplayService displayService;

    public ChatListener(FileConfiguration config, TitleDisplayService displayService) {
        this.config = config;
        this.displayService = displayService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!config.getBoolean("display.chat.enabled", true)) {
            return;
        }

        try {
            String display = displayService.equippedTitle(event.getPlayer()).map(title -> title.display() + ChatColor.RESET).orElse("");
            String format = config.getString("display.chat.format", "{display} {player}: {message}")
                    .replace("{display}", display)
                    .replace("{player}", "%1$s")
                    .replace("{message}", "%2$s");
            event.setFormat(format);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
