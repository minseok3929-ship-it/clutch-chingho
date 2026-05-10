# ClutchTitle Architecture

ClutchTitle is a Paper 1.21.11 plugin for manually granting colored player titles.

## Main flow

- `/칭호` opens the player's owned-title GUI.
- `/칭호 지급 <닉네임> <칭호이름> [색상]` grants a title manually.
- The color argument may be a supported color name or a legacy Minecraft color code.
- When the color is omitted, the title is granted with `§f` white.
- Each grant stores `title_name`, `color_code`, and `display` separately.

## Display targets

The equipped title display is reused by all visible surfaces:

- GUI item name and lore
- chat prefix
- TAB list name
- overhead name tag through a scoreboard team prefix

## Storage

SQLite tables:

- `player_titles`: one row for each manually granted title.
- `equipped_titles`: one equipped title id per player.

The initial data model intentionally stores each player grant as an independent title record so future features can add expiration, grant reasons, grant admins, categories, or per-player variants without changing the command flow.
