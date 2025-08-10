package com.yaesaraki.studyLife.session;

import com.yaesaraki.studyLife.StudyLifePlugin;
import com.yaesaraki.studyLife.data.PlayerDataManager;
import com.yaesaraki.studyLife.notifier.Notifier;
import com.yaesaraki.studyLife.util.Log;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PomodoroSession {

    public static PomodoroSession getActiveSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public enum State {
        FOCUS, BREAK, DONE
    }

    private static final Map<UUID, PomodoroSession> activeSessions = new HashMap<>();

    private final Player player;
    private final UUID uuid;
    private final RegionScheduler scheduler;

    private final int totalRounds;
    private int currentRound;
    private State state;
    private long secondsRemaining;
    private long focusStartSeconds;

    private ScheduledTask task;
    private final FileConfiguration config;

    public PomodoroSession(Player player, int totalRounds) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.totalRounds = totalRounds;
        this.scheduler = Bukkit.getRegionScheduler();
        this.config = StudyLifePlugin.getInstance().getConfig();
    }

    public void start() {
        if (activeSessions.containsKey(uuid)) {
            Notifier.sendActionBar(player, "你已经在进行一个番茄钟喵~");
            return;
        }
        currentRound = 1;
        state = State.FOCUS;
        secondsRemaining = getFocusSeconds();
        focusStartSeconds = secondsRemaining;

        activeSessions.put(uuid, this);

        sendTitle("start-title", "start-subtitle");
//        Notifier.showHologram(player, formatTime(secondsRemaining));
        scheduleTickLoop();
    }

    private void scheduleTickLoop() {
        task = player.getScheduler().runAtFixedRate(
                StudyLifePlugin.getInstance(),
                scheduled -> second(),
                null,
                20L, 20L // 延迟1秒启动，每秒执行一次
        );
    }

    private void second() {
        if (--secondsRemaining <= 0) {
            switch (state) {
                case FOCUS -> {
                    recordFocusTime();
                    sendTitle("break-start-title", "break-start-subtitle");
                    state = State.BREAK;
                    secondsRemaining = getBreakSeconds();
                }
                case BREAK -> {
                    sendTitle("break-end-title", "break-end-subtitle");
                    currentRound++;
                    if (currentRound > totalRounds) {
                        sendTitle("done-title", "done-subtitle");
                        stop();
                        return;
                    }
                    sendTitle("next-title", "next-subtitle");
                    state = State.FOCUS;
                    secondsRemaining = getFocusSeconds();
                    focusStartSeconds = secondsRemaining;
                }
            }
        }

        if (state == State.BREAK && secondsRemaining == 10) {
            sendTitle("break-end-warning-title", "break-end-warning-subtitle");
            Notifier.sendActionBar(player, config.getString("messages.break-end-warning-actionbar", "喵~ 还有 10 秒就要回去学习了喵！"));
        }

        Log.debug("second " + secondsRemaining);
        sendBossBar();
        sendActionBar();
        sendHologram();
    }

    public void stop() {
        if (state == State.FOCUS) {
            recordFocusTime();
        }
        if (task != null) task.cancel();
        Notifier.clearHologram(player);
        Notifier.hideBossBar(player);
        activeSessions.remove(uuid);
    }

    private void recordFocusTime() {
        long completedSeconds = focusStartSeconds - secondsRemaining;
        PlayerDataManager.addStudyTime(uuid, completedSeconds);
    }

    public static void interrupt(Player player) {
        PomodoroSession session = activeSessions.get(player.getUniqueId());
        if (session != null && session.state == State.FOCUS) {
            session.sendTitle("interrupt-title", "interrupt-subtitle");
            session.stop();
        }
    }

    public static boolean isFocusing(UUID uuid) {
        PomodoroSession s = activeSessions.get(uuid);
        return s != null && s.state == State.FOCUS;
    }

    private String formatTime(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private long getFocusSeconds() {
        return config.getInt("focus-time", 25) * 60L;
    }

    private long getBreakSeconds() {
        return config.getInt("break-time", 5) * 60L;
    }

    private void sendTitle(String titleKey, String subtitleKey) {
        String title = config.getString("messages." + titleKey, "");
        String subtitle = config.getString("messages." + subtitleKey, "")
                .replace("%round%", String.valueOf(currentRound))
                .replace("%total%", String.valueOf(totalRounds));
        Notifier.sendTitle(player, title, subtitle);
    }

    private void sendBossBar() {
        double percent = (double) secondsRemaining / (state == State.FOCUS ? getFocusSeconds() : getBreakSeconds());
        Notifier.showBossBar(player, formatTime(secondsRemaining), percent);
    }

    private void sendActionBar() {
        Notifier.sendActionBar(player, formatTime(secondsRemaining));
    }

    private void sendHologram() {
        String stateTextKey = switch (state) {
            case FOCUS -> "hologram.state.focus";
            case BREAK -> "hologram.state.break";
            case DONE -> "hologram.state.done";
        };

        String stateText = config.getString(stateTextKey, "未知状态喵");
        String line1Template = config.getString("hologram.line1", "状态：%state%（剩余 %time%）");
        String line2Template = config.getString("hologram.line2", "累计专注：%total_minutes% 分钟");

        String formattedTime = formatTime(secondsRemaining);
        long totalSeconds = PlayerDataManager.getStudyTime(uuid);
        long totalMinutes = totalSeconds / 60;

        String line1 = line1Template
                .replace("%state%", stateText)
                .replace("%time%", formattedTime);

        String line2 = line2Template
                .replace("%total_seconds%", String.valueOf(totalSeconds))
                .replace("%total_minutes%", String.valueOf(totalMinutes));

        Notifier.showMultilineHologram(player, List.of(line1, line2));
    }
}
