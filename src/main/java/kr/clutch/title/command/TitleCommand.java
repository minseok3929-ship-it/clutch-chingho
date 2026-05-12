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
        if (args.length > 0 && args[0].equalsIgnoreCase("지급")) {
            return grant(sender, args);
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("확인")) {
            return list(sender, args);
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("삭제")) {
            return delete(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.message(config, "player-only"));
            return true;
        }
        if (!player.hasPermission("clutch.title.use")) {
            player.sendMessage(MessageUtil.message(config, "no-permission"));
            return true;
        }

        try {
            titleGui.open(player);
        } catch (SQLException exception) {
            player.sendMessage("§8[CLUTCH] §c칭호 목록을 불러오지 못했습니다.");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean grant(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            sender.sendMessage(MessageUtil.message(config, "no-permission"));
            return true;
        }
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(MessageUtil.message(config, "usage-grant"));
            return true;
        }

        Optional<String> color = args.length == 4 ? TitleColor.parse(args[3]) : Optional.of(TitleColor.defaultColor());
        if (color.isEmpty()) {
            sender.sendMessage(MessageUtil.message(config, "invalid-color"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            TitleService.GrantResult result = titleService.grant(target, args[2], color.get());
            if (!result.granted()) {
                sender.sendMessage(MessageUtil.message(config, "already-owned"));
                return true;
            }
            sender.sendMessage(MessageUtil.apply(MessageUtil.message(config, "granted"), "display_name", result.title().displayName()));
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.sendMessage(MessageUtil.apply(MessageUtil.message(config, "received"), "display_name", result.title().displayName()));
            }
        } catch (SQLException exception) {
            sender.sendMessage("§8[CLUTCH] §c칭호 지급 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean list(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            sender.sendMessage(MessageUtil.message(config, "no-permission"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(MessageUtil.message(config, "usage-check"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            List<PlayerTitle> titles = titleService.ownedTitles(target.getUniqueId());
            sender.sendMessage("§8[CLUTCH] §f" + args[1] + "의 보유 칭호:");
            if (titles.isEmpty()) {
                sender.sendMessage("- §7없음");
                return true;
            }
            for (PlayerTitle title : titles) {
                sender.sendMessage("- " + title.displayName() + (title.equipped() ? " §a(장착중)" : ""));
            }
        } catch (SQLException exception) {
            sender.sendMessage("§8[CLUTCH] §c칭호 목록을 불러오지 못했습니다.");
            exception.printStackTrace();
        }
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            sender.sendMessage(MessageUtil.message(config, "no-permission"));
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(MessageUtil.message(config, "usage-delete"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            if (!titleService.removeTitle(target, args[2])) {
                sender.sendMessage(MessageUtil.message(config, "not-owned-delete"));
                return true;
            }
            sender.sendMessage(MessageUtil.message(config, "deleted"));
        } catch (SQLException exception) {
            sender.sendMessage("§8[CLUTCH] §c칭호 삭제 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
        return true;
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
