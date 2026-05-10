package kr.clutch.title.service;

import kr.clutch.title.database.TitleRepository;
import kr.clutch.title.model.PlayerTitle;
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

    public PlayerTitle grant(OfflinePlayer target, String titleName, String colorCode) throws SQLException {
        UUID uuid = target.getUniqueId();
        String name = target.getName() == null ? uuid.toString() : target.getName();
        String display = TitleColor.display(titleName, colorCode);
        PlayerTitle title = repository.grant(uuid, name, titleName, colorCode, display);
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null) {
            displayService.refresh(onlinePlayer);
        }
        return title;
    }

    public List<PlayerTitle> ownedTitles(UUID playerUuid) throws SQLException {
        return repository.findByPlayer(playerUuid);
    }

    public Optional<PlayerTitle> equippedTitle(UUID playerUuid) throws SQLException {
        return repository.findEquipped(playerUuid);
    }

    public boolean equip(Player player, long titleId) throws SQLException {
        Optional<PlayerTitle> title = repository.findById(titleId);
        if (title.isEmpty() || !title.get().playerUuid().equals(player.getUniqueId())) {
            return false;
        }
        repository.equip(player.getUniqueId(), titleId);
        displayService.refresh(player);
        return true;
    }

    public void unequip(Player player) throws SQLException {
        repository.unequip(player.getUniqueId());
        displayService.refresh(player);
    }
}
