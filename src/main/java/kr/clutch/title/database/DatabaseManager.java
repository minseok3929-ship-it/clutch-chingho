package kr.clutch.title.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager implements AutoCloseable {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void open() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.file", "titles.db"));
        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_titles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_uuid TEXT NOT NULL,
                        player_name TEXT NOT NULL,
                        title_name TEXT NOT NULL,
                        color_code TEXT NOT NULL,
                        display TEXT NOT NULL,
                        granted_at INTEGER NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS equipped_titles (
                        player_uuid TEXT PRIMARY KEY,
                        title_id INTEGER NOT NULL,
                        equipped_at INTEGER NOT NULL,
                        FOREIGN KEY (title_id) REFERENCES player_titles(id) ON DELETE CASCADE
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_player_titles_uuid ON player_titles(player_uuid)");
        }
    }

    public Connection connection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
