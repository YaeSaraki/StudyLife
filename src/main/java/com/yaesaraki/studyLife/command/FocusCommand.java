package com.yaesaraki.studyLife.command;

import com.yaesaraki.studyLife.StudyLifePlugin;
import com.yaesaraki.studyLife.session.PomodoroSession;
import com.yaesaraki.studyLife.ui.FocusMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FocusCommand implements CommandExecutor {

    public static final Map<UUID, Integer> lastUsedRounds = new HashMap<>();
    private static final int DEFAULT_ROUNDS = 4;

    public FocusCommand(StudyLifePlugin plugin) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("喵~ 只有玩家能使用这个命令！");
            return true;
        }

        if (args.length == 0) {
            FocusMenu.open(player); // GUI 菜单中不需要 plugin，因为 UI 是单例/静态初始化
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            int last = lastUsedRounds.getOrDefault(player.getUniqueId(), DEFAULT_ROUNDS);
            new PomodoroSession(player, last).start();
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            PomodoroSession session = PomodoroSession.getActiveSession(player);
            if (session != null) {
                session.stop();
                player.sendMessage("§c你已终止当前专注任务。");
            } else {
                player.sendMessage("§7你当前没有正在进行的专注任务喵~");
            }
            return true;
        }


        player.sendMessage("§e用法: /focus [start]");
        return true;
    }
}