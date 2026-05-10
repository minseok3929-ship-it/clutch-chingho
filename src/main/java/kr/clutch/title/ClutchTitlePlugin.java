package kr.clutch.title;

import kr.clutch.title.command.TitleCommand;
import kr.clutch.title.database.DatabaseManager;
import kr.clutch.title.database.TitleRepository;
import kr.clutch.title.gui.TitleGui;
import kr.clutch.title.listener.ChatListener;
import kr.clutch.title.listener.PlayerConnectionListener;
import kr.clutch.title.listener.TitleGuiListener;
import kr.clutch.title.service.TitleDisplayService;
import kr.clutch.title.service.TitleService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class ClutchTitlePlugin extends JavaPlugin {
    private DatabaseManager databaseManager;
    private TitleDisplayService displayService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.open();
        } catch (SQLException exception) {
            getLogger().severe("SQLite 데이터베이스를 열 수 없습니다: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        TitleRepository titleRepository = new TitleRepository(databaseManager.connection());
        displayService = new TitleDisplayService(getConfig());
        displayService.setRepository(titleRepository);
        TitleService titleService = new TitleService(titleRepository, displayService);
        TitleGui titleGui = new TitleGui(getConfig(), titleService);

        TitleCommand titleCommand = new TitleCommand(getConfig(), titleService, titleGui);
        PluginCommand command = getCommand("칭호");
        if (command != null) {
            command.setExecutor(titleCommand);
            command.setTabCompleter(titleCommand);
        }

        getServer().getPluginManager().registerEvents(new TitleGuiListener(getConfig(), titleService, titleGui), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(displayService), this);
        getServer().getPluginManager().registerEvents(new ChatListener(getConfig(), displayService), this);
        displayService.refreshAll();
    }

    @Override
    public void onDisable() {
        if (displayService != null) {
            getServer().getOnlinePlayers().forEach(displayService::clear);
        }
        if (databaseManager != null) {
            try {
                databaseManager.close();
            } catch (SQLException exception) {
                getLogger().warning("SQLite 데이터베이스를 닫을 수 없습니다: " + exception.getMessage());
            }
        }
    }
}
