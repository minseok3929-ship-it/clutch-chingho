package kr.clutch.title.listener;

import kr.clutch.title.model.Title;
import kr.clutch.title.service.TitleService;
import kr.clutch.title.ticket.TitleTicketFactory;
import kr.clutch.title.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;

public final class TitleTicketListener implements Listener {
    private final FileConfiguration config;
    private final TitleService titleService;
    private final TitleTicketFactory ticketFactory;

    public TitleTicketListener(FileConfiguration config, TitleService titleService, TitleTicketFactory ticketFactory) {
        this.config = config;
        this.titleService = titleService;
        this.ticketFactory = ticketFactory;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Byte ticket = container.get(ticketFactory.ticketKey(), PersistentDataType.BYTE);
        String titleName = container.get(ticketFactory.titleNameKey(), PersistentDataType.STRING);
        if (ticket == null || ticket != (byte) 1 || titleName == null || titleName.isBlank()) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        try {
            TitleService.ClaimResult result = titleService.claimTicket(player, titleName);
            if (!result.titleExists()) {
                player.sendMessage("§8[CLUTCH] §c존재하지 않는 칭호입니다.");
                return;
            }
            Title title = result.title();
            if (!result.granted()) {
                player.sendMessage(MessageUtil.message(config, "already-owned"));
                return;
            }
            item.setAmount(item.getAmount() - 1);
            player.sendMessage(MessageUtil.apply(MessageUtil.message(config, "received"), "display_name", title.displayName()));
            player.sendTitle("§8CLUTCH", title.displayName(), 10, 60, 20);
        } catch (SQLException exception) {
            player.sendMessage("§8[CLUTCH] §c칭호 획득 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
    }
}
