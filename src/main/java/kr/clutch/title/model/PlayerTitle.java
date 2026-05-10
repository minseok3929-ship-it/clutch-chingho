package kr.clutch.title.model;

import java.time.Instant;
import java.util.UUID;

public record PlayerTitle(
        long id,
        UUID playerUuid,
        String playerName,
        String titleName,
        String colorCode,
        String display,
        Instant grantedAt
) {
}
