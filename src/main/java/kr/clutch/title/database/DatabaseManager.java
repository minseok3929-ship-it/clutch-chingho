package kr.clutch.title.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

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
            recreateDevelopmentSchemaIfNeeded(statement);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS titles (
                        title_name TEXT PRIMARY KEY,
                        color TEXT NOT NULL,
                        display_name TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_titles (
                        uuid TEXT NOT NULL,
                        title_name TEXT NOT NULL,
                        equipped INTEGER NOT NULL DEFAULT 0,
                        granted_at INTEGER NOT NULL,
                        PRIMARY KEY(uuid, title_name),
                        FOREIGN KEY(title_name) REFERENCES titles(title_name) ON DELETE CASCADE
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_player_titles_uuid ON player_titles(uuid)");
        }
    }

    private void recreateDevelopmentSchemaIfNeeded(Statement statement) throws SQLException {
        if (tableExists("player_titles") && !columns("player_titles").contains("uuid")) {
            statement.execute("DROP TABLE IF EXISTS equipped_titles");
            statement.execute("DROP TABLE IF EXISTS player_titles");
            statement.execute("DROP TABLE IF EXISTS titles");
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private Set<String> columns(String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        return columns;
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
