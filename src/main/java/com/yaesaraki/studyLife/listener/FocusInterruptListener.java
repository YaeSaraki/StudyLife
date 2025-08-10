package com.yaesaraki.studyLife.listener;

import com.yaesaraki.studyLife.session.PomodoroSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FocusInterruptListener implements Listener {

    private final Map<UUID, Location> lastPositions = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 如果不在专注状态中，不检测
        if (!PomodoroSession.isFocusing(uuid)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // 如果坐标未变化，不视为移动（小角度转身等忽略）
        if (to == null || (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        // 移动了，打断当前 session
        PomodoroSession.interrupt(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PomodoroSession session = PomodoroSession.getActiveSession(event.getPlayer());
        if (session != null) {
            session.stop();
        }
    }
}