package com.yaesaraki.studyLife.util;

import com.yaesaraki.studyLife.StudyLifePlugin;
import com.yaesaraki.studyLife.ui.MenuButton;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UIHelper {

    public static YamlConfiguration menuConfig;

    public static void loadMenus() {
        File file = new File(StudyLifePlugin.getInstance().getDataFolder(), "menus.yml");
        if (!file.exists()) {
            StudyLifePlugin.getInstance().saveResource("menus.yml", false);
        }
        menuConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static List<String> color(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }

    public static String get(String path) {
        return color(menuConfig.getString(path, "§c[缺失配置：" + path + "]"));
    }

    public static List<String> getList(String path) {
        return color(menuConfig.getStringList(path));
    }

    public static ItemStack buildButton(Material mat, String labelPath, String lorePath, String... placeholders) {
        String label = get(labelPath);
        List<String> lore = getList(lorePath);

        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String key = placeholders[i];
            String value = placeholders[i + 1];
            label = label.replace(key, value);
            for (int j = 0; j < lore.size(); j++) {
                lore.set(j, lore.get(j).replace(key, value));
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(label);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }


    public static List<MenuButton> loadMenuButtons(String path, String... placeholders) {
        List<MenuButton> buttons = new ArrayList<>();
        List<Map<?, ?>> rawButtons = menuConfig.getMapList(path + ".buttons");

        for (Map<?, ?> raw : rawButtons) {
            String id = String.valueOf(raw.get("id"));
            int slot = (int) raw.get("slot");
            Material mat = Material.getMaterial(String.valueOf(raw.get("material")));

            String label = color(String.valueOf(raw.get("label")));
            List<String> lore = raw.containsKey("lore") ? color((List<String>) raw.get("lore")) : null;

            // 替换占位符
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                String key = placeholders[i];
                String value = placeholders[i + 1];
                label = label.replace(key, value);
                if (lore != null) {
                    for (int j = 0; j < lore.size(); j++) {
                        lore.set(j, lore.get(j).replace(key, value));
                    }
                }
            }

            ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(label);
                if (lore != null) meta.setLore(lore);
                item.setItemMeta(meta);
            }

            buttons.add(new MenuButton(id, slot, item));
        }

        return buttons;
    }
}