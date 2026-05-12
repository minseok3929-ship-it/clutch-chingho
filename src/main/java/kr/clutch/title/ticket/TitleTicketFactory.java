package kr.clutch.title.ticket;

import kr.clutch.title.model.Title;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class TitleTicketFactory {
    private final FileConfiguration config;
    private final NamespacedKey ticketKey;
    private final NamespacedKey titleNameKey;

    public TitleTicketFactory(Plugin plugin, FileConfiguration config) {
        this.config = config;
        this.ticketKey = new NamespacedKey(plugin, "clutch_title_ticket");
        this.titleNameKey = new NamespacedKey(plugin, "clutch_title_name");
    }

    public ItemStack create(Title title, int amount) {
        ItemStack item = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(config.getString("ticket.display-name", "§6칭호 획득권"));
        int customModelData = config.getInt("ticket.custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        meta.setLore(List.of(
                "§7우클릭 시 칭호를 획득합니다.",
                "§f획득 칭호:",
                title.displayName()
        ));
        meta.getPersistentDataContainer().set(ticketKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(titleNameKey, PersistentDataType.STRING, title.titleName());
        item.setItemMeta(meta);
        return item;
    }

    public NamespacedKey ticketKey() {
        return ticketKey;
    }

    public NamespacedKey titleNameKey() {
        return titleNameKey;
    }
}
