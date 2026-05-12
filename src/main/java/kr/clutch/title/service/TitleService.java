package kr.clutch.title.service;

import kr.clutch.title.database.TitleRepository;
import kr.clutch.title.model.PlayerTitle;
import kr.clutch.title.model.Title;
import kr.clutch.title.util.TitleColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TitleService {
    private final TitleRepository repository;
    private final TitleDisplayService displayService;

    public TitleService(TitleRepository repository, TitleDisplayService displayService) {
        this.repository = repository;
        this.displayService = displayService;
    }

    public Title upsertTitle(String titleName, String color) throws SQLException {
        Title title = new Title(titleName, color, TitleColor.displayName(titleName, color));
        repository.upsertTitle(title);
        displayService.refreshAll();
        return title;
    }

    public GrantResult grant(OfflinePlayer target, String titleName, String color) throws SQLException {
        Title title = upsertTitle(titleName, color);
        boolean granted = repository.grant(target.getUniqueId(), titleName);
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null) {
            displayService.refresh(onlinePlayer);
        }
        return new GrantResult(title, granted);
    }

    public ClaimResult claimTicket(Player player, String titleName) throws SQLException {
        Optional<Title> title = repository.findTitle(titleName);
        if (title.isEmpty()) {
            return ClaimResult.missingTitle();
        }
        boolean granted = repository.grant(player.getUniqueId(), titleName);
        if (granted) {
            displayService.refresh(player);
        }
        return new ClaimResult(title.get(), granted, true);
    }

    public List<PlayerTitle> ownedTitles(UUID playerUuid) throws SQLException {
        return repository.findByPlayer(playerUuid);
    }

    public Optional<PlayerTitle> equippedTitle(UUID playerUuid) throws SQLException {
        return repository.findEquipped(playerUuid);
    }

    public Optional<Title> title(String titleName) throws SQLException {
        return repository.findTitle(titleName);
    }

    public boolean equip(Player player, String titleName) throws SQLException {
        boolean equipped = repository.equip(player.getUniqueId(), titleName);
        if (equipped) {
            displayService.refresh(player);
        }
        return equipped;
    }

    public void unequip(Player player) throws SQLException {
        repository.unequip(player.getUniqueId());
        displayService.refresh(player);
    }

    public boolean removeTitle(OfflinePlayer target, String titleName) throws SQLException {
        boolean removed = repository.remove(target.getUniqueId(), titleName);
        Player onlinePlayer = target.getPlayer();
        if (removed && onlinePlayer != null) {
            displayService.refresh(onlinePlayer);
        }
        return removed;
    }

    public record GrantResult(Title title, boolean granted) {
    }

    public record ClaimResult(Title title, boolean granted, boolean titleExists) {
        public static ClaimResult missingTitle() {
            return new ClaimResult(null, false, false);
        }
    }
}
