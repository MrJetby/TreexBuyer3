package me.jetby.treexBuyer;

import com.jodexindustries.jguiwrapper.common.JGuiInitializer;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treexBuyer.command.AdminCommand;
import me.jetby.treexBuyer.configurations.Config;
import me.jetby.treexBuyer.configurations.Items;
import me.jetby.treexBuyer.functions.AutoBuy;
import me.jetby.treexBuyer.functions.Coefficient;
import me.jetby.treexBuyer.menus.CommandRegistrar;
import me.jetby.treexBuyer.menus.Loader;
import me.jetby.treexBuyer.storage.JSON;
import me.jetby.treexBuyer.storage.SQL;
import me.jetby.treexBuyer.storage.Storage;
import me.jetby.treexBuyer.storage.Yaml;
import me.jetby.treexBuyer.tools.Logger;
import me.jetby.treexBuyer.tools.Metrics;
import me.jetby.treexBuyer.tools.TreexBuyerPlaceholders;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;

@Getter
public final class Main extends JavaPlugin {

    private final Loader menuLoader = new Loader(this, getDataFolder());
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey("treexbuyer", "item");
    private Economy economy;
    @Getter
    private static Main instance;
    private Items items;

    @Setter
    private Storage storage;
    private Config cfg;
    private Coefficient coefficient;
    private AutoBuy autoBuy;
    private TreexBuyerPlaceholders treexBuyerPlaceholders;

    public static final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void onEnable() {
        instance = this;

        JGuiInitializer.init(this);

        new Metrics(this, 25141);

        cfg = new Config();
        cfg.load();

        loadStorage();

        items = new Items();
        items.load();
        menuLoader.load();

        autoBuy = new AutoBuy(this);
        autoBuy.start();

        coefficient = new Coefficient(this);


        if (!setupEconomy()) {
            Logger.error("Vault with economy plugin not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Logger.warn("PlaceholderAPI not found. Placeholders are not being working");
        } else {
            treexBuyerPlaceholders = new TreexBuyerPlaceholders(this);
            treexBuyerPlaceholders.register();
        }

        CommandRegistrar.createCommands(this);

        PluginCommand treexbuyer = getCommand("treexbuyer");
        if (treexbuyer != null)
            treexbuyer.setExecutor(new AdminCommand(this));
    }

    public void loadStorage() {
        switch (cfg.getStorageType()) {
            case "MYSQL", "SQLITE":
                storage = new SQL(this);
                break;
            case "JSON":
                storage = new JSON();
                break;
            default:
                storage = new Yaml(this);
        }
        storage.load();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        this.economy = rsp.getProvider();
        return true;
    }

    @Override
    public void onDisable() {
        if (treexBuyerPlaceholders != null) {
            if (treexBuyerPlaceholders.isRegistered()) {
                treexBuyerPlaceholders.unregister();
            }
        }

        CommandRegistrar.unregisterAll(this);

        if (storage != null) storage.save();

    }
}
