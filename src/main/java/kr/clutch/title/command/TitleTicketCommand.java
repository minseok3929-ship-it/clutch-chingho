package kr.clutch.title.command;

import kr.clutch.title.model.Title;
import kr.clutch.title.service.TitleService;
import kr.clutch.title.ticket.TitleTicketFactory;
import kr.clutch.title.util.MessageUtil;
import kr.clutch.title.util.TitleColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TitleTicketCommand implements CommandExecutor, TabCompleter {
    private final FileConfiguration config;
    private final TitleService titleService;
    private final TitleTicketFactory ticketFactory;

    public TitleTicketCommand(FileConfiguration config, TitleService titleService, TitleTicketFactory ticketFactory) {
        this.config = config;
        this.titleService = titleService;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clutch.title.admin")) {
            MessageUtil.send(sender, config, "no-permission");
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, config, "player-only");
            return true;
        }
        if (args.length != 4 || !args[0].equalsIgnoreCase("생성")) {
            MessageUtil.send(sender, config, "usage-ticket");
            return true;
        }

        Optional<String> color = TitleColor.parse(args[2]);
        if (color.isEmpty()) {
            MessageUtil.send(sender, config, "invalid-color");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            MessageUtil.send(sender, config, "invalid-amount");
            return true;
        }
        if (amount < 1) {
            MessageUtil.send(sender, config, "invalid-amount");
            return true;
        }

        try {
            Title title = titleService.upsertTitle(args[1], color.get());
            ItemStack ticket = ticketFactory.create(title, amount);
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(ticket);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            MessageUtil.send(sender, config, "ticket-created", Map.of(
                    "title", title.titleName(),
                    "display", title.displayName(),
                    "display_name", title.displayName(),
                    "color", title.color(),
                    "amount", String.valueOf(amount)
            ));
        } catch (SQLException exception) {
            MessageUtil.send(sender, config, "error-ticket-create");
            exception.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return "생성".startsWith(args[0]) ? List.of("생성") : List.of();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("생성")) {
            return TitleCommand.COLOR_NAMES.stream().filter(name -> name.startsWith(args[2].toLowerCase())).toList();
        }
        return List.of();
    }
}
