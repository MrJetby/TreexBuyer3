package me.jetby.treexBuyer.command;

import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.CommandRegistrar;
import me.jetby.treexBuyer.menus.JGui;
import me.jetby.treexBuyer.storage.JSON;
import me.jetby.treexBuyer.storage.SQL;
import me.jetby.treexBuyer.storage.Storage;
import me.jetby.treexBuyer.storage.Yaml;
import me.jetby.treexBuyer.tools.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class AdminCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final Storage storage;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage( );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (args[0].equalsIgnoreCase("open")) {
            if (args.length != 2) {
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Команда доступна только игрокам.");
                return true;
            }
            new JGui(plugin.getMenuLoader( ).getMenus( ).get(args[1]), plugin, player).open(player);
        }

        switch (args[0]) {
            case "open": {
                if (args.length == 1) {
                    sender.sendMessage(TextUtil.colorize("&#EF473AUsage: /treexbuyer open <menu> <player>"));
                    break;
                }
                if (sender instanceof Player player) {
                    if (args.length == 2) {
                        new JGui(plugin.getMenuLoader( ).getMenus( ).get(args[1]), plugin, player).open(player);
                    } else {
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target != null) {
                            new JGui(plugin.getMenuLoader( ).getMenus( ).get(args[1]), plugin, Bukkit.getPlayer(args[2])).open(target);
                        } else {
                            sender.sendMessage(TextUtil.colorize("&#EF473APlayer not found"));
                        }
                    }
                } else {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target != null) {
                        new JGui(plugin.getMenuLoader( ).getMenus( ).get(args[1]), plugin, Bukkit.getPlayer(args[2])).open(target);
                    } else {
                        sender.sendMessage(TextUtil.colorize("&#EF473APlayer not found"));
                    }
                }
                break;
            }
            case "reload": {
                sender.sendMessage(TextUtil.colorize("&#82FB16Successfully reloaded, took only " + reload( ) + " ms."));
                break;
            }
            case "score": {
                switch (args[1]) {
                    case "give": {
                        if (args.length <= 3) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AUsage: /treexbuyer score give <player> <amount>"));
                            break;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AAmount argument should be a number"));
                            break;
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            String string = "OfflinePlayer:" + args[2];
                            UUID offlinePlayer = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                            int score = storage.getScore(offlinePlayer);
                            storage.setScore(offlinePlayer, score + amount);
                        } else {
                            int score = storage.getScore(target.getUniqueId( ));
                            storage.setScore(target.getUniqueId( ), score + amount);
                        }

                        sender.sendMessage(TextUtil.colorize("&#82FB16Successfully given " + amount + " scores to " + args[2]));

                        break;
                    }
                    case "take": {
                        if (args.length <= 3) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AUsage: /treexbuyer score take <player> <amount>"));
                            break;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AAmount argument should be a number"));
                            break;
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            String string = "OfflinePlayer:" + args[2];
                            UUID offlinePlayer = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                            int score = storage.getScore(offlinePlayer);
                            if (score>=amount) {
                                storage.setScore(offlinePlayer, score - amount);
                            } else {
                                storage.setScore(offlinePlayer, 0);
                            }
                        } else {
                            int score = storage.getScore(target.getUniqueId( ));
                            if (score>=amount) {
                                storage.setScore(target.getUniqueId(), score - amount);
                            } else {
                                storage.setScore(target.getUniqueId(), 0);
                            }
                        }
                        sender.sendMessage(TextUtil.colorize("&#82FB16Successfully taken " + amount + " scores from " + args[2]));
                        break;
                    }
                    case "set": {
                        if (args.length <= 3) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AUsage: /treexbuyer score set <player> <amount>"));
                            break;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(TextUtil.colorize("&#EF473AAmount argument should be a number"));
                            break;
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            String string = "OfflinePlayer:" + args[2];
                            UUID offlinePlayer = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                            storage.setScore(offlinePlayer, Math.max(amount, 0));
                        } else {
                            storage.setScore(target.getUniqueId(), Math.max(amount, 0));
                        }
                        sender.sendMessage(TextUtil.colorize("&#82FB16Successfully set " + amount + " scores to " + args[2]));
                        break;
                    }
                    default: {
                        sender.sendMessage(TextUtil.colorize(""));
                    }
                }
                break;
            }
        }


        return false;
    }

    private long reload() {
        long start = System.currentTimeMillis( );
        try {
            try {
                storage.save(true);
            } catch (Exception e) {
                return System.currentTimeMillis( ) - start;
            }

                plugin.getCfg( ).load( );

                switch (plugin.getCfg( ).getStorageType( )) {
                    case "MYSQL", "SQLITE":
                        plugin.setStorage(new SQL(plugin));
                        break;
                    case "JSON":
                        plugin.setStorage(new JSON( ));
                        break;
                    default:
                        plugin.setStorage(new Yaml(plugin));

                }
                storage.load( );

                plugin.getItems( ).load( );
                plugin.getMenuLoader( ).load( );

                plugin.getAutoBuy( ).stop( );
                plugin.getAutoBuy( ).start( );


                CommandRegistrar.unregisterAll(plugin);
                CommandRegistrar.createCommands(plugin);


        } catch (Exception ex) {
            ex.printStackTrace( );
        }

        return System.currentTimeMillis( ) - start;
    }

    static final List<String> completions = new ArrayList<>( );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            completions.clear( );

            if (args.length == 1) {
                completions.add("open");
                completions.add("score");
                completions.add("reload");
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
                if (player.hasPermission("treexbuyer.admin")) {
                    completions.addAll(plugin.getMenuLoader( ).getMenus( ).keySet());
                }
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("score")) {
                if (player.hasPermission("treexbuyer.admin")) {
                    completions.add("take");
                    completions.add("give");
                    completions.add("set");
                }
            }
        }

        return completions.stream( )
                .filter(menuId -> menuId.toLowerCase( ).startsWith(args[args.length-1].toLowerCase( )))
                .collect(Collectors.toList( ));
    }
}
