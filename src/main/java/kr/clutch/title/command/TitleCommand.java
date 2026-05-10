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
    private static final List<String> COLOR_NAMES = List.of(
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

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.message(config, "player-only"));
            return true;
        }
        if (!player.hasPermission("clutchtitle.use")) {
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
        if (!sender.hasPermission("clutchtitle.grant")) {
            sender.sendMessage(MessageUtil.message(config, "no-permission"));
            return true;
        }
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(MessageUtil.message(config, "usage-grant"));
            return true;
        }

        Optional<String> colorCode = args.length == 4 ? TitleColor.parse(args[3]) : Optional.of(TitleColor.defaultColorCode());
        if (colorCode.isEmpty()) {
            sender.sendMessage(MessageUtil.message(config, "invalid-color"));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        try {
            PlayerTitle granted = titleService.grant(target, args[2], colorCode.get());
            String message = MessageUtil.message(config, "granted");
            sender.sendMessage(MessageUtil.apply(MessageUtil.apply(message, "player", args[1]), "display", granted.display()));
        } catch (SQLException exception) {
            sender.sendMessage("§8[CLUTCH] §c칭호 지급 중 오류가 발생했습니다.");
            exception.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("지급"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("지급")) {
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
