package me.jetby.treexBuyer.configurations;

import lombok.Getter;
import me.jetby.treexBuyer.functions.Boost;
import me.jetby.treexBuyer.tools.FileLoader;
import me.jetby.treexBuyer.tools.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Config {

    String storageType;
    boolean yamlForceSave;

    int scores;
    double coefficient;
    int maxCoefficient;
    int defaultCoefficient;
    boolean boosters_except_legal_coefficient;

    String enable;
    String disable;

    String host;
    int port;
    String database;
    String username;
    String password;

    String itemsPrices;

    int autoBuyDelay;
    List<String> autoBuyActions;
    List<String> disabledWorlds;

    private final Map<String, Boost> boosts = new HashMap<>();

    public void load() {
        FileConfiguration configuration = FileLoader.getFileConfiguration("config.yml");

        storageType = configuration.getString("storage.type", "yaml").toUpperCase();
        yamlForceSave = configuration.getBoolean("storage.yaml-force-save", false);


        host = configuration.getString("storage.host");
        port = configuration.getInt("storage.port");
        database = configuration.getString("storage.database");
        username = configuration.getString("storage.username");
        password = configuration.getString("storage.password");

        autoBuyDelay = configuration.getInt("autobuy.delay", 60);
        autoBuyActions = configuration.getStringList("autobuy.actions");
        disabledWorlds = configuration.getStringList("autobuy.disabled-worlds");
        enable = TextUtil.colorize(configuration.getString("autobuy.status.enable", "&aВключён"));
        disable = TextUtil.colorize(configuration.getString("autobuy.status.disable", "&cВыключен"));

        scores = configuration.getInt("score-to-multiplier-ratio.scores", 100);
        coefficient = configuration.getDouble("score-to-multiplier-ratio.coefficient", 0.01);
        maxCoefficient = configuration.getInt("max-legal-coefficient", 3);
        defaultCoefficient = configuration.getInt("default-coefficient", 1);
        boosters_except_legal_coefficient = configuration.getBoolean("boosters_except_legal_coefficient", false);

        loadBoosts(configuration);

        itemsPrices = configuration.getString("items-prices-file", "priceItem.yml");

    }
    public void loadBoosts(FileConfiguration configuration) {
        boosts.clear();
        ConfigurationSection boosterSection = configuration.getConfigurationSection("booster");
        if (boosterSection != null) {
            for (String key : boosterSection.getKeys(false)) {
                String permission = boosterSection.getString(key + ".permission");
                double coefficient = boosterSection.getDouble(key + ".external-coefficient", 0.0);
                boosts.put(key, new Boost(key, permission, coefficient));
            }
        }
    }

}
