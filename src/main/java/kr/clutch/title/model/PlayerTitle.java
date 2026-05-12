package kr.clutch.title.model;

import java.time.Instant;
import java.util.UUID;

public record PlayerTitle(
        UUID playerUuid,
        String titleName,
        String color,
        String displayName,
        boolean equipped,
        Instant grantedAt
) {
}
