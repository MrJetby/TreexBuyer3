package me.jetby.treexBuyer.storage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    boolean load();

    boolean save();

    String type();

    boolean playerExists(UUID uuid);

    void setScore(UUID uuid, String key, int value);

    int getScore(UUID uuid, String key);

    void setAutoBuyItems(UUID uuid, List<String> items);

    List<String> getAutoBuyItems(UUID uuid);

    void setAutoBuyStatus(UUID uuid, boolean status);

    boolean getAutoBuyStatus(UUID uuid);

    String getTopName(int number);

    int getTopScore(int number);


}
