package kr.clutch.title.database;

import kr.clutch.title.model.PlayerTitle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public PlayerTitle grant(UUID playerUuid, String playerName, String titleName, String colorCode, String display) throws SQLException {
        long grantedAt = Instant.now().toEpochMilli();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player_titles(player_uuid, player_name, title_name, color_code, display, granted_at) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, playerName);
            statement.setString(3, titleName);
            statement.setString(4, colorCode);
            statement.setString(5, display);
            statement.setLong(6, grantedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new PlayerTitle(keys.getLong(1), playerUuid, playerName, titleName, colorCode, display, Instant.ofEpochMilli(grantedAt));
                }
            }
        }
        throw new SQLException("Failed to read generated title id.");
    }

    public List<PlayerTitle> findByPlayer(UUID playerUuid) throws SQLException {
        List<PlayerTitle> titles = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, player_uuid, player_name, title_name, color_code, display, granted_at FROM player_titles WHERE player_uuid = ? ORDER BY granted_at DESC, id DESC"
        )) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    titles.add(map(resultSet));
                }
            }
        }
        return titles;
    }

    public Optional<PlayerTitle> findById(long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, player_uuid, player_name, title_name, color_code, display, granted_at FROM player_titles WHERE id = ?"
        )) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public void equip(UUID playerUuid, long titleId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO equipped_titles(player_uuid, title_id, equipped_at) VALUES (?, ?, ?) "
                        + "ON CONFLICT(player_uuid) DO UPDATE SET title_id = excluded.title_id, equipped_at = excluded.equipped_at"
        )) {
            statement.setString(1, playerUuid.toString());
            statement.setLong(2, titleId);
            statement.setLong(3, Instant.now().toEpochMilli());
            statement.executeUpdate();
        }
    }

    public void unequip(UUID playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM equipped_titles WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    public Optional<PlayerTitle> findEquipped(UUID playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT pt.id, pt.player_uuid, pt.player_name, pt.title_name, pt.color_code, pt.display, pt.granted_at "
                        + "FROM equipped_titles et JOIN player_titles pt ON et.title_id = pt.id WHERE et.player_uuid = ?"
        )) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private PlayerTitle map(ResultSet resultSet) throws SQLException {
        return new PlayerTitle(
                resultSet.getLong("id"),
                UUID.fromString(resultSet.getString("player_uuid")),
                resultSet.getString("player_name"),
                resultSet.getString("title_name"),
                resultSet.getString("color_code"),
                resultSet.getString("display"),
                Instant.ofEpochMilli(resultSet.getLong("granted_at"))
        );
    }
}
