package kr.clutch.title.listener;

import kr.clutch.title.service.TitleDisplayService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public final class PlayerConnectionListener implements Listener {
    private final TitleDisplayService displayService;

    public PlayerConnectionListener(TitleDisplayService displayService) {
        this.displayService = displayService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            displayService.refresh(event.getPlayer());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        displayService.clear(event.getPlayer());
    }
}
