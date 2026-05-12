package kr.clutch.title.service;

import kr.clutch.title.database.TitleRepository;
import kr.clutch.title.model.PlayerTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.SQLException;
import java.util.Optional;

public final class TitleDisplayService {
    private static final String TEAM_PREFIX = "ct_";

    private final FileConfiguration config;
    private TitleRepository repository;

    public TitleDisplayService(FileConfiguration config) {
        this.config = config;
    }

    public void setRepository(TitleRepository repository) {
        this.repository = repository;
    }

    public Optional<PlayerTitle> equippedTitle(Player player) throws SQLException {
        if (repository == null) {
            return Optional.empty();
        }
        return repository.findEquipped(player.getUniqueId());
    }

    public void refresh(Player player) throws SQLException {
        Optional<PlayerTitle> title = equippedTitle(player);
        String displayName = title.map(PlayerTitle::displayName).orElse("");
        updateNameTag(player, displayName);
        updateTab(player, displayName);
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                refresh(player);
            } catch (SQLException exception) {
                Bukkit.getLogger().warning("Failed to refresh title display for " + player.getName() + ": " + exception.getMessage());
            }
        }
    }

    public void clear(Player player) {
        clearNameTag(player);
        player.setPlayerListName(player.getName());
    }

    private void updateNameTag(Player player, String displayName) {
        if (!config.getBoolean("display.nametag.enabled", true)) {
            clearNameTag(player);
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = teamName(player);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        team.setPrefix(displayName.isBlank() ? "" : displayName + ChatColor.RESET + " ");
    }

    private void clearNameTag(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName(player));
        if (team != null) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
    }

    private void updateTab(Player player, String displayName) {
        if (!config.getBoolean("display.tab.enabled", true) || displayName.isBlank()) {
            player.setPlayerListName(player.getName());
            return;
        }

        String format = config.getString("display.tab.format", "{display_name} {player}");
        player.setPlayerListName(format
                .replace("{display}", displayName)
                .replace("{display_name}", displayName)
                .replace("{player}", player.getName()) + ChatColor.RESET);
    }

    private String teamName(Player player) {
        return TEAM_PREFIX + player.getUniqueId().toString().replace("-", "").substring(0, 13);
    }
}
