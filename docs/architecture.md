# ClutchTitle Architecture

ClutchTitle is a Paper 1.21.11 plugin for manually granting colored player titles and title-claim tickets.

## Main flow

- `/칭호` opens the player's owned-title GUI.
- `/칭호 지급 <닉네임> <칭호이름> [색상]` grants a title manually.
- `/칭호 확인 <닉네임>` lists a player's owned titles and marks the equipped title.
- `/칭호 삭제 <닉네임> <칭호이름>` removes a player's title and clears it if equipped.
- `/칭호권 생성 <칭호이름> <색상> <개수>` creates title-claim ticket items.
- The color argument may be a supported color name, a legacy Minecraft color code, or a `#RRGGBB` HEX value.
- When the manual grant color is omitted, the title is granted with `§f` white.
- The title name itself is the unique title identifier.

## Display targets

The stored `display_name` value is reused by all visible surfaces:

- GUI item name and lore
- chat prefix
- TAB list name
- overhead name tag through a scoreboard team prefix

## Storage

SQLite tables:

- `titles`: one row per unique `title_name`, with `color` and `display_name`.
- `player_titles`: one row per player/title pair, with an `equipped` flag.

The data model keeps the title name as the stable identifier so future metadata can be added without exposing separate title keys to admins.
