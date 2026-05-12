package kr.clutch.title.database;

import kr.clutch.title.model.PlayerTitle;
import kr.clutch.title.model.Title;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TitleRepository {
    private final Connection connection;

    public TitleRepository(Connection connection) {
        this.connection = connection;
    }

    public void upsertTitle(Title title) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO titles(title_name, color, display_name) VALUES (?, ?, ?) "
                        + "ON CONFLICT(title_name) DO UPDATE SET color = excluded.color, display_name = excluded.display_name"
        )) {
            statement.setString(1, title.titleName());
            statement.setString(2, title.color());
            statement.setString(3, title.displayName());
            statement.executeUpdate();
        }
    }

    public Optional<Title> findTitle(String titleName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT title_name, color, display_name FROM titles WHERE title_name = ?"
        )) {
            statement.setString(1, titleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTitle(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean grant(UUID playerUuid, String titleName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_titles(uuid, title_name, equipped, granted_at) VALUES (?, ?, 0, ?)"
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, titleName);
            statement.setLong(3, Instant.now().toEpochMilli());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean hasTitle(UUID playerUuid, String titleName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM player_titles WHERE uuid = ? AND title_name = ?"
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, titleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<PlayerTitle> findByPlayer(UUID playerUuid) throws SQLException {
        List<PlayerTitle> titles = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pt.uuid, pt.title_name, t.color, t.display_name, pt.equipped, pt.granted_at "
                        + "FROM player_titles pt JOIN titles t ON pt.title_name = t.title_name "
                        + "WHERE pt.uuid = ? ORDER BY pt.equipped DESC, pt.granted_at DESC, pt.title_name ASC"
        )) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    titles.add(mapPlayerTitle(resultSet));
                }
            }
        }
        return titles;
    }

    public Optional<PlayerTitle> findPlayerTitle(UUID playerUuid, String titleName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pt.uuid, pt.title_name, t.color, t.display_name, pt.equipped, pt.granted_at "
                        + "FROM player_titles pt JOIN titles t ON pt.title_name = t.title_name "
                        + "WHERE pt.uuid = ? AND pt.title_name = ?"
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, titleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapPlayerTitle(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<PlayerTitle> findEquipped(UUID playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pt.uuid, pt.title_name, t.color, t.display_name, pt.equipped, pt.granted_at "
                        + "FROM player_titles pt JOIN titles t ON pt.title_name = t.title_name "
                        + "WHERE pt.uuid = ? AND pt.equipped = 1"
        )) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapPlayerTitle(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean equip(UUID playerUuid, String titleName) throws SQLException {
        if (!hasTitle(playerUuid, titleName)) {
            return false;
        }
        connection.setAutoCommit(false);
        try (PreparedStatement clear = connection.prepareStatement("UPDATE player_titles SET equipped = 0 WHERE uuid = ?");
             PreparedStatement equip = connection.prepareStatement("UPDATE player_titles SET equipped = 1 WHERE uuid = ? AND title_name = ?")) {
            clear.setString(1, playerUuid.toString());
            clear.executeUpdate();
            equip.setString(1, playerUuid.toString());
            equip.setString(2, titleName);
            boolean updated = equip.executeUpdate() > 0;
            connection.commit();
            return updated;
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void unequip(UUID playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE player_titles SET equipped = 0 WHERE uuid = ?")) {
            statement.setString(1, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    public boolean remove(UUID playerUuid, String titleName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM player_titles WHERE uuid = ? AND title_name = ?")) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, titleName);
            return statement.executeUpdate() > 0;
        }
    }

    private Title mapTitle(ResultSet resultSet) throws SQLException {
        return new Title(
                resultSet.getString("title_name"),
                resultSet.getString("color"),
                resultSet.getString("display_name")
        );
    }

    private PlayerTitle mapPlayerTitle(ResultSet resultSet) throws SQLException {
        return new PlayerTitle(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("title_name"),
                resultSet.getString("color"),
                resultSet.getString("display_name"),
                resultSet.getInt("equipped") == 1,
                Instant.ofEpochMilli(resultSet.getLong("granted_at"))
        );
    }
}
