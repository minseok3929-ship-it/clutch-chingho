package kr.clutch.title.command;

import kr.clutch.title.gui.TitleGui;
import kr.clutch.title.model.PlayerTitle;
import kr.clutch.title.service.TitleService;
import kr.clutch.title.util.MessageUtil;
import kr.clutch.title.util.TitleColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TitleCommand implements CommandExecutor, TabCompleter {
    public static final List<String> COLOR_NAMES = List.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    );

    private final FileConfiguration config;
    private final TitleService titleService;
    private final TitleGui titleGui;

    public TitleCommand(FileConfiguration config, TitleService titleService, TitleGui titleGui) {
        this.config = config;
        this.titleService = titleService;
        this.titleGui = titleGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return openGui(sender);
        }
        if (args[0].equalsIgnoreCase("지급")) {
            return grant(sender, args);
        }
        if (args[0].equalsIgnoreCase("확인")) {
            return list(sender, args);
        }
        if (args[0].equalsIgnoreCase("삭제")) {
            return delete(sender, args);
        }

        sendHelp(sender);
        return true;
    }

    private boolean openGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, config, "player-only");
            return true;
        }
        if (!player.hasPermission("clutch.title.use")) {
            MessageUtil.send(player, config, "no-permission");
            return true;
        }

        try {
            titleGui.open(player);
        } catch (SQLException exception) {
            MessageUtil.send(player, config, "error-list");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean grant(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            MessageUtil.send(sender, config, "no-permission");
            return true;
        }
        if (args.length != 4) {
            MessageUtil.send(sender, config, "usage-grant");
            return true;
        }

        Optional<String> color = TitleColor.parse(args[3]);
        if (color.isEmpty()) {
            MessageUtil.send(sender, config, "invalid-color");
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            TitleService.GrantResult result = titleService.grant(target, args[2], color.get());
            if (!result.granted()) {
                MessageUtil.send(sender, config, "already-owned");
                return true;
            }
            Map<String, String> placeholders = Map.of(
                    "player", args[1],
                    "title", result.title().titleName(),
                    "display", result.title().displayName(),
                    "display_name", result.title().displayName(),
                    "color", result.title().color()
            );
            MessageUtil.send(sender, config, "granted", placeholders);
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                MessageUtil.send(onlineTarget, config, "received", placeholders);
            }
        } catch (SQLException exception) {
            MessageUtil.send(sender, config, "error-grant");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            MessageUtil.send(sender, config, "no-permission");
            return true;
        }
        if (args.length != 2) {
            MessageUtil.send(sender, config, "usage-check");
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            List<PlayerTitle> titles = titleService.ownedTitles(target.getUniqueId());
            MessageUtil.sendRaw(sender, "§f<player>의 보유 칭호:", Map.of("player", args[1]));
            if (titles.isEmpty()) {
                MessageUtil.sendRaw(sender, "§7- 없음");
                return true;
            }
            for (PlayerTitle title : titles) {
                MessageUtil.sendRaw(sender, "§f- <display>§f" + (title.equipped() ? " §a(장착중)" : ""), Map.of("display", title.displayName()));
            }
        } catch (SQLException exception) {
            MessageUtil.send(sender, config, "error-list");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            MessageUtil.send(sender, config, "no-permission");
            return true;
        }
        if (args.length != 3) {
            MessageUtil.send(sender, config, "usage-delete");
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            if (!titleService.removeTitle(target, args[2])) {
                MessageUtil.send(sender, config, "not-owned-delete");
                return true;
            }
            MessageUtil.send(sender, config, "deleted");
        } catch (SQLException exception) {
            MessageUtil.send(sender, config, "error-delete");
            exception.printStackTrace();
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.send(sender, config, "help-title");
        MessageUtil.send(sender, config, "help-grant");
        MessageUtil.send(sender, config, "help-check");
        MessageUtil.send(sender, config, "help-delete");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("지급", "확인", "삭제"), args[0]);
        }
        if (args.length == 2 && List.of("지급", "확인", "삭제").contains(args[0])) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> startsWithIgnoreCase(name, args[1])).toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("지급")) {
            return filter(COLOR_NAMES, args[3]);
        }
        return List.of();
    }

    private List<String> filter(List<String> candidates, String input) {
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (startsWithIgnoreCase(candidate, input)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private boolean startsWithIgnoreCase(String value, String input) {
        return value.toLowerCase().startsWith(input.toLowerCase());
    }
}
