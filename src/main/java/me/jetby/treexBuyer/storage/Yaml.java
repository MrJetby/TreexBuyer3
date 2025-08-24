package me.jetby.treexBuyer.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.tools.FileLoader;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class Yaml implements Storage {
    final Main plugin;
    final Map<UUID, Data> cache = new HashMap<>();
    final FileConfiguration configuration = FileLoader.getFileConfiguration("storage.yml");

    @Override
    public boolean load() {
        final boolean[] status = {false};

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance( ), ()-> {
            long start = System.currentTimeMillis();

            try {
                for (String key : configuration.getKeys(false)) {
                    UUID uuid = UUID.fromString(key);
                    int score = configuration.getInt(key+".score", 0);
                    boolean autoBuy = configuration.getBoolean(key+".autoBuy", false);
                    List<String> items = configuration.getStringList(key+".autoBuyItems");
                    Data data = new Data();
                    data.setUuid(uuid);
                    data.setScore(score);
                    data.setAutoBuy(autoBuy);
                    data.setAutoBuyItems(items);
                    cache.put(uuid, data);
                }
                Logger.success("Данные из storage.yml были загружены за " + (System.currentTimeMillis() - start) + " мс");
                status[0] = true;

            } catch (Exception e) {
                e.printStackTrace();
                status[0] = false;
            }
        });
        return status[0];
    }

    @Override
    public boolean save(boolean async) {
        final boolean[] status = {false};
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance( ), ()-> {

                try {
                    for (Map.Entry<UUID, Data> entry : cache.entrySet()) {
                        Data data = entry.getValue();
                        String key = data.getUuid().toString();
                        configuration.set(key + ".score", data.getScore());
                        configuration.set(key + ".autoBuy", data.isAutoBuy());
                        configuration.set(key + ".autoBuyItems", data.getAutoBuyItems());
                    }

                    configuration.save(FileLoader.getFile("storage.yml"));
                    status[0] = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    status[0] = false;
                }
            });
        } else {
            long start = System.currentTimeMillis();

            try {
                for (Map.Entry<UUID, Data> entry : cache.entrySet()) {
                    Data data = entry.getValue();
                    String key = data.getUuid().toString();
                    configuration.set(key + ".score", data.getScore());
                    configuration.set(key + ".autoBuy", data.isAutoBuy());
                    configuration.set(key + ".autoBuyItems", data.getAutoBuyItems());
                }

                configuration.save(FileLoader.getFile("storage.yml"));
                Logger.success("Данные в storage.yml были сохранены за " + (System.currentTimeMillis() - start) + " мс");
                status[0] = true;

            } catch (Exception e) {
                e.printStackTrace();
                status[0] = false;
            }
        }
        return status[0];
    }

    @Override
    public String type() {
        return "YAML";
    }

    @Override
    public boolean playerExists(UUID uuid) {
        Data data = cache.get(uuid);
        return data != null;
    }

    @Override
    public void setScore(UUID uuid, int score) {
        if (playerExists(uuid)) {
            Data data = cache.get(uuid);
            data.setScore(score);
        } else {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(score);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of());
            cache.put(uuid, data);
        }
        if (plugin.getCfg().isYamlForceSave()) save(true);

    }

    @Override
    public int getScore(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of());
            cache.put(uuid, data);
            if (plugin.getCfg().isYamlForceSave()) save(true);
            return 0;
        }
        Data data = cache.get(uuid);
        return data.getScore();
    }

    @Override
    public void setAutoBuyItems(UUID uuid, List<String> items) {
        if (!playerExists(uuid)) {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(items);
            cache.put(uuid, data);
            if (plugin.getCfg().isYamlForceSave()) save(true);
            return;
        }
        Data data = cache.get(uuid);
        data.setAutoBuyItems(items);
        if (plugin.getCfg().isYamlForceSave()) save(true);
    }

    @Override
    public List<String> getAutoBuyItems(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of());
            cache.put(uuid, data);
            if (plugin.getCfg().isYamlForceSave()) save(true);
            return List.of();
        }
        Data data = cache.get(uuid);
        return data.getAutoBuyItems();
    }

    @Override
    public void setAutoBuyStatus(UUID uuid, boolean status) {
        if (!playerExists(uuid)) {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(status);
            data.setAutoBuyItems(List.of());
            cache.put(uuid, data);
            if (plugin.getCfg().isYamlForceSave()) save(true);
            return;
        }
        Data data = cache.get(uuid);
        data.setAutoBuy(status);
        if (plugin.getCfg().isYamlForceSave()) save(true);
    }

    @Override
    public boolean getAutoBuyStatus(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data();
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of());
            cache.put(uuid, data);
            return false;
        }
        Data data = cache.get(uuid);
        return data.isAutoBuy();
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class Data {
        UUID uuid;
        boolean autoBuy;
        List<String> autoBuyItems;
        int score;
    }


}
