package kr.clutch.title.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public final class TitleGuiHolder implements InventoryHolder {
    private final Map<Integer, String> titleNamesBySlot = new HashMap<>();
    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void bind(int slot, String titleName) {
        titleNamesBySlot.put(slot, titleName);
    }

    public String titleName(int slot) {
        return titleNamesBySlot.get(slot);
    }
}
