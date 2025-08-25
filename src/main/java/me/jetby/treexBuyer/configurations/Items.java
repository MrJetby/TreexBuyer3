package me.jetby.treexBuyer.configurations;

import lombok.Getter;
import me.jetby.treexBuyer.tools.FileLoader;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Items {

    private final Map<Material, ItemData> itemValues = new HashMap<>();

    public void load() {
        FileConfiguration config = FileLoader.getFileConfiguration("prices.yml");

        for (String key : config.getKeys(false)) {
            double price = config.getDouble(key + ".price", 0);
            int addScores = config.getInt(key + ".add-scores", 0);
            itemValues.put(Material.valueOf(key), new ItemData(price, addScores));
        }

    }

    public record ItemData(double price, int score) {
    }
}
