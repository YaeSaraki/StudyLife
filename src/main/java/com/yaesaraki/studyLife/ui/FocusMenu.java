package com.yaesaraki.studyLife.ui;

import com.yaesaraki.studyLife.StudyLifePlugin;
import com.yaesaraki.studyLife.session.PomodoroSession;
import com.yaesaraki.studyLife.util.UIHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FocusMenu implements Listener {

    private static final Map<UUID, Integer> focusAmount = new HashMap<>();
    private static final int MIN = 1, MAX = 10;
    private static boolean registered = false;

    public static void open(Player player) {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new FocusMenu(), StudyLifePlugin.getInstance());
            registered = true;
        }

        UUID uuid = player.getUniqueId();
        focusAmount.putIfAbsent(uuid, 4);
        int amount = focusAmount.get(uuid);

        String title = UIHelper.get("menus.focus.title");
        int size = UIHelper.menuConfig.getInt("menus.focus.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        List<MenuButton> buttons = UIHelper.loadMenuButtons("menus.focus", "%amount%", String.valueOf(amount));
        for (MenuButton button : buttons) {
            inv.setItem(button.slot(), button.item());
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(UIHelper.get("menus.focus.title"))) return;

        event.setCancelled(true);
        UUID uuid = player.getUniqueId();
        int amount = focusAmount.getOrDefault(uuid, 4);

        // 确定点的按钮功能
        int clicked = event.getSlot();
        List<MenuButton> buttons = UIHelper.loadMenuButtons("menus.focus", "%amount%", String.valueOf(amount));

        for (MenuButton button : buttons) {
            if (button.slot() == clicked) {
                switch (button.id()) {
                    case "decrease" -> {
                        amount = Math.max(MIN, amount - 1);
                        focusAmount.put(uuid, amount);
                        open(player);
                    }
                    case "increase" -> {
                        amount = Math.min(MAX, amount + 1);
                        focusAmount.put(uuid, amount);
                        open(player);
                    }
                    case "start" -> {
                        player.closeInventory();
                        new PomodoroSession(player, amount).start();
                    }
                    // 其他按钮无操作
                }
                break;
            }
        }
    }
}