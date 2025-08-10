package com.yaesaraki.studyLife;

import com.yaesaraki.studyLife.command.FocusCommand;
import com.yaesaraki.studyLife.data.PlayerDataManager;
import com.yaesaraki.studyLife.listener.FocusInterruptListener;
import com.yaesaraki.studyLife.util.UIHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StudyLifePlugin extends JavaPlugin {

    private static StudyLifePlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        //  初始化数据库
        String dbPath = getDataFolder().getAbsolutePath() + "/data.db";
        PlayerDataManager.init(dbPath);

        //  注册命令执行器
        this.getCommand("focus").setExecutor(new FocusCommand(this));

        //  注册事件监听器
        Bukkit.getPluginManager().registerEvents(new FocusInterruptListener(), this);

        //  加载配置
        saveDefaultConfig();
        UIHelper.loadMenus();

        getLogger().info("StudyLife 插件已启动，喵呜~");
    }

    @Override
    public void onDisable() {
        getLogger().info("StudyLife 插件已关闭，记得休息喵~");
    }

    public static StudyLifePlugin getInstance() {
        return instance;
    }
}