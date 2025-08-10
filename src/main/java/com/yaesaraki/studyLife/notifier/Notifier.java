package com.yaesaraki.studyLife.notifier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.*;

public class Notifier {

    private static final Map<UUID, ArmorStand> singleHolograms = new HashMap<>();
    private static final Map<UUID, List<ArmorStand>> multilineHolograms = new HashMap<>();
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();

    // === TITLE ===
    public static void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(title, subtitle, 10, 40, 10);
    }

    // === ACTIONBAR ===
    public static void sendActionBar(Player player, String msg) {
        player.sendActionBar(msg);
    }

    // === BOSSBAR ===
    public static void showBossBar(Player player, String title, double progress) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.computeIfAbsent(uuid, id -> {
            BossBar b = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SEGMENTED_20);
            b.addPlayer(player);
            return b;
        });

        bar.setVisible(true);
        bar.setTitle(title);
        bar.setProgress(Math.max(0, Math.min(1, progress)));
    }

    public static void hideBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAll();
        }
    }

    // === 单行 HOLOGRAM（计时显示等） ===
    public static void showHologram(Player player, String text) {
        UUID uuid = player.getUniqueId();
        ArmorStand stand = singleHolograms.get(uuid);
        Location loc = player.getLocation().clone().add(0, 2.6, 0);

        if (stand == null || stand.isDead() || !stand.isValid()) {
            stand = spawnHologram(loc, text);
            singleHolograms.put(uuid, stand);
        } else {
            stand.setCustomName(text);
            stand.teleportAsync(loc);
        }
    }

    // === 多行 HOLOGRAM（状态 + 总时间） ===
    public static void showMultilineHologram(Player player, List<String> lines) {
        clearMultilineHologram(player);

        UUID uuid = player.getUniqueId();
        Location baseLoc = player.getLocation().clone().add(0, 2.6, 0);
        List<ArmorStand> stands = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location loc = baseLoc.clone().add(0, 0.25 * (lines.size() - i - 1), 0);
            ArmorStand stand = spawnHologram(loc, line);
            stands.add(stand);
        }

        multilineHolograms.put(uuid, stands);
    }

    private static ArmorStand spawnHologram(Location loc, String text) {
        return loc.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setInvisible(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setGravity(false);
            armorStand.setSmall(true);
            armorStand.setCustomName(text);
        });
    }

    public static void clearHologram(Player player) {
        UUID uuid = player.getUniqueId();

        ArmorStand single = singleHolograms.remove(uuid);
        if (single != null && !single.isDead()) {
            single.remove();
        }

        clearMultilineHologram(player);
    }

    private static void clearMultilineHologram(Player player) {
        UUID uuid = player.getUniqueId();
        List<ArmorStand> stands = multilineHolograms.remove(uuid);

        if (stands != null) {
            for (ArmorStand stand : stands) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }
    }
}