package me.jetby.treexBuyer.storage;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.tools.FileLoader;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@RequiredArgsConstructor
public class Yaml implements Storage {
    final Main plugin;
    final Map<UUID, Data> cache = new HashMap<>( );
    final FileConfiguration configuration = FileLoader.getFileConfiguration("storage.yml");

    @Override
    public boolean load() {
        cache.clear();
        boolean status = false;

        long start = System.currentTimeMillis( );

        try {
            for (String key : configuration.getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                int score = configuration.getInt(key + ".score", 0);
                boolean autoBuy = configuration.getBoolean(key + ".autoBuy", false);
                List<String> items = configuration.getStringList(key + ".autoBuyItems");
                Data data = new Data( );
                data.setUuid(uuid);
                data.setScore(score);
                data.setAutoBuy(autoBuy);
                data.setAutoBuyItems(items);
                cache.put(uuid, data);
            }
            Logger.success("Данные из storage.yml были загружены за " + (System.currentTimeMillis( ) - start) + " мс");
            status = true;

        } catch (Exception e) {
            Logger.error("Error with loading from the storage: "+ e);
        }

        return status;
    }

    @Override
    public boolean save() {
        boolean status = false;

        long start = System.currentTimeMillis( );
        try {
            for (Map.Entry<UUID, Data> entry : cache.entrySet( )) {
                Data data = entry.getValue( );
                String key = data.getUuid( ).toString( );
                configuration.set(key + ".score", data.getScore( ));
                configuration.set(key + ".autoBuy", data.isAutoBuy( ));
                configuration.set(key + ".autoBuyItems", data.getAutoBuyItems( ));
            }

            configuration.save(FileLoader.getFile("storage.yml"));
            Logger.success("Данные в storage.yml были сохранены за " + (System.currentTimeMillis( ) - start) + " мс");
            status = true;

        } catch (Exception e) {
            Logger.error("Error with saving to the storage: "+ e);
        }


        return status;
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
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(score);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of( ));
            cache.put(uuid, data);
        }
        if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error( "Failed to save score for UUID: " + uuid);}

    }

    @Override
    public int getScore(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(List.of( ));
            cache.put(uuid, data);
            if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error("Failed to save score for UUID: " + uuid);}

            return 0;
        }
        Data data = cache.get(uuid);
        return data.getScore( );
    }

    @Override
    public void setAutoBuyItems(UUID uuid, List<String> items) {
        if (!playerExists(uuid)) {
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(new ArrayList<>(items));
            cache.put(uuid, data);
            if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error("Failed to save score for UUID: " + uuid);}

            return;
        }
        Data data = cache.get(uuid);
        data.setAutoBuyItems(items);
        if (plugin.getCfg( ).isYamlForceSave( )) {if (!save())Logger.error("Failed to save score for UUID: " + uuid);}

    }

    @Override
    public List<String> getAutoBuyItems(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(new ArrayList<>( ));
            cache.put(uuid, data);
            if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error( "Failed to save score for UUID: " + uuid);}

            return List.of( );
        }
        Data data = cache.get(uuid);
        return data.getAutoBuyItems( );
    }

    @Override
    public void setAutoBuyStatus(UUID uuid, boolean status) {
        if (!playerExists(uuid)) {
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(status);
            data.setAutoBuyItems(new ArrayList<>( ));
            cache.put(uuid, data);
            if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error("Failed to save score for UUID: " + uuid);}

            return;
        }
        Data data = cache.get(uuid);
        data.setAutoBuy(status);
        if (plugin.getCfg( ).isYamlForceSave( )) {if (!save()) Logger.error("Failed to save score for UUID: " + uuid);}

    }

    @Override
    public boolean getAutoBuyStatus(UUID uuid) {
        if (!playerExists(uuid)) {
            Data data = new Data( );
            data.setUuid(uuid);
            data.setScore(0);
            data.setAutoBuy(false);
            data.setAutoBuyItems(new ArrayList<>( ));
            cache.put(uuid, data);
            return false;
        }
        Data data = cache.get(uuid);
        return data.isAutoBuy( );
    }

    @lombok.Data
    public static class Data {
        UUID uuid;
        boolean autoBuy;
        List<String> autoBuyItems;
        int score;
    }


}
