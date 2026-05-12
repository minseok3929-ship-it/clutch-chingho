package kr.clutch.title.listener;

import kr.clutch.title.gui.TitleGui;
import kr.clutch.title.gui.TitleGuiHolder;
import kr.clutch.title.service.TitleService;
import kr.clutch.title.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.sql.SQLException;

public final class TitleGuiListener implements Listener {
    private final FileConfiguration config;
    private final TitleService titleService;
    private final TitleGui titleGui;

    public TitleGuiListener(FileConfiguration config, TitleService titleService, TitleGui titleGui) {
        this.config = config;
        this.titleService = titleService;
        this.titleGui = titleGui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TitleGuiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int size = event.getInventory().getSize();
        try {
            if (event.getRawSlot() == size - 1) {
                titleService.unequip(player);
                MessageUtil.send(player, config, "unequipped");
                titleGui.open(player);
                return;
            }

            String titleName = holder.titleName(event.getRawSlot());
            if (titleName == null) {
                return;
            }
            if (!titleService.equip(player, titleName)) {
                MessageUtil.send(player, config, "not-owned");
                return;
            }
            titleService.equippedTitle(player.getUniqueId()).ifPresent(title -> MessageUtil.send(player, config, "equipped", java.util.Map.of(
                    "title", title.titleName(),
                    "display", title.displayName(),
                    "display_name", title.displayName(),
                    "color", title.color()
            )));
            titleGui.open(player);
        } catch (SQLException exception) {
            MessageUtil.send(player, config, "error-change");
            exception.printStackTrace();
        }
    }
}
