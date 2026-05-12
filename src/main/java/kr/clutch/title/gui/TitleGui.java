package kr.clutch.title.gui;

import kr.clutch.title.model.PlayerTitle;
import kr.clutch.title.service.TitleService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class TitleGui {
    private final FileConfiguration config;
    private final TitleService titleService;

    public TitleGui(FileConfiguration config, TitleService titleService) {
        this.config = config;
        this.titleService = titleService;
    }

    public void open(Player player) throws SQLException {
        List<PlayerTitle> titles = titleService.ownedTitles(player.getUniqueId());
        if (titles.isEmpty()) {
            player.sendMessage(config.getString("gui.empty-message", "§8[CLUTCH] §c보유한 칭호가 없습니다."));
            return;
        }

        TitleGuiHolder holder = new TitleGuiHolder();
        int size = config.getInt("gui.size", 54);
        Inventory inventory = Bukkit.createInventory(holder, size, config.getString("gui.title", "§8칭호 목록"));
        holder.setInventory(inventory);

        int maxTitleSlots = Math.min(size - 1, titles.size());
        for (int slot = 0; slot < maxTitleSlots; slot++) {
            PlayerTitle title = titles.get(slot);
            inventory.setItem(slot, titleItem(title));
            holder.bind(slot, title.titleName());
        }
        inventory.setItem(size - 1, unequipItem());
        player.openInventory(inventory);
    }

    private ItemStack titleItem(PlayerTitle title) {
        ItemStack item = new ItemStack(title.equipped() ? Material.ENCHANTED_BOOK : Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title.displayName());
        List<String> lore = new ArrayList<>();
        lore.add("§7칭호 이름: §f" + title.titleName());
        lore.add("§7표시: " + title.displayName());
        lore.add("");
        lore.add(title.equipped() ? "§a현재 장착 중입니다." : "§e클릭하여 장착합니다.");
        meta.setLore(lore);
        if (title.equipped()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack unequipItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c칭호 해제");
        meta.setLore(List.of("§7현재 장착 중인 칭호를 해제합니다."));
        item.setItemMeta(meta);
        return item;
    }
}
