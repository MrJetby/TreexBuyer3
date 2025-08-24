package me.jetby.treexBuyer.storage;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SQL implements Storage {

    private boolean mySql = false;
    private Connection connection;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private static final String CREATE_DATABASE = "CREATE TABLE IF NOT EXISTS players (uuid TEXT NOT NULL, score INTEGER DEFAULT 0, autoBuy BOOLEAN DEFAULT false, autoBuyItems TEXT DEFAULT '', PRIMARY KEY (uuid));";
    private static final String GET_ALL_PLAYERS = "SELECT * FROM players;";
    private static final String INSERT_PLAYER = "INSERT INTO players (uuid, score, autoBuy, autoBuyItems) VALUES (?, ?, ?, ?) ON CONFLICT(uuid) DO UPDATE SET score = ?, autoBuy = ?, autoBuyItems = ?;";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    private String mysqlHost = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDatabase = "your_db";
    private String mysqlUser = "user";
    private String mysqlPassword = "password";

    private boolean globalStatus = true;

    private final Main plugin;

    private static class PlayerData {
        int score = 0;
        boolean autoBuy = false;
        List<String> autoBuyItems = new ArrayList<>();

        PlayerData(int score, boolean autoBuy, List<String> autoBuyItems) {
            this.score = score;
            this.autoBuy = autoBuy;
            this.autoBuyItems = new ArrayList<>(autoBuyItems);
        }

        PlayerData() {}
    }

    private Connection connect() throws SQLException {
        if (mySql) {
            try {
                Class.forName(MYSQL_DRIVER);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL driver not found", e);
            }
            return DriverManager.getConnection(
                    "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
                    mysqlUser, mysqlPassword);
        } else {
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/storage.db");
        }
    }

    @Override
    public boolean load() {
        if (globalStatus) {
            try {
                connection = connect( );
                createTables( );
                loadCacheAsync( );
                return true;
            } catch (SQLException e) {
                Logger.error("Ошибка инициализации базы данных: " + e);
                return false;
            }
        }
        return false;
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_DATABASE);
            Logger.success("Таблица игроков успешно создана или уже существует.");
        } catch (SQLException e) {
            Logger.error("Ошибка при создании таблицы игроков: " + e.getMessage());
        }
    }

    private void loadCacheAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long start = System.currentTimeMillis();
            try (PreparedStatement ps = connection.prepareStatement(GET_ALL_PLAYERS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int score = rs.getInt("score");
                    boolean autoBuy = rs.getBoolean("autoBuy");
                    String itemsStr = rs.getString("autoBuyItems");
                    List<String> items = itemsStr == null || itemsStr.isEmpty() ? new ArrayList<>() : Arrays.asList(itemsStr.split(","));
                    cache.put(uuid, new PlayerData(score, autoBuy, items));
                }
                Logger.success("Данные из storage.db были загружены за " + (System.currentTimeMillis() - start) + " мс");
            } catch (SQLException e) {
                Logger.error("Ошибка загрузки кэша: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean save(boolean async) {
        globalStatus = false;
        boolean[] status = {false};
        Runnable saveTask = () -> {
            try (Connection conn = connection; PreparedStatement ps = conn.prepareStatement(INSERT_PLAYER)) {
                conn.setAutoCommit(false);
                for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
                    UUID uuid = entry.getKey();
                    PlayerData data = entry.getValue();
                    String itemsStr = data.autoBuyItems.stream().collect(Collectors.joining(","));
                    ps.setString(1, uuid.toString());
                    ps.setInt(2, data.score);
                    ps.setBoolean(3, data.autoBuy);
                    ps.setString(4, itemsStr);
                    ps.setInt(5, data.score);
                    ps.setBoolean(6, data.autoBuy);
                    ps.setString(7, itemsStr);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                Logger.success("Кэш сохранён в БД.");
                status[0] = true;
            } catch (SQLException e) {
                Logger.error("Ошибка сохранения кэша: " + e.getMessage());
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    Logger.error("Ошибка отката транзакции: " + ex.getMessage());
                }
                status[0] = false;
            } finally {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.setAutoCommit(true);
                    }
                } catch (SQLException e) {
                    Logger.error("Ошибка восстановления автокоммита: " + e.getMessage());
                }
            }
            globalStatus = true;
        };
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, saveTask);
        } else {
            saveTask.run();
        }
        return status[0];
    }

    @Override
    public String type() {
        return mySql ? "MYSQL" : "SQLITE";
    }

    @Override
    public boolean playerExists(UUID uuid) {
        return cache.containsKey(uuid);
    }

    @Override
    public void setScore(UUID uuid, int score) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        data.score = score;
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public int getScore(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        return data.score;
    }

    @Override
    public void setAutoBuyItems(UUID uuid, List<String> items) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        data.autoBuyItems = new ArrayList<>(items);
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public List<String> getAutoBuyItems(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        return new ArrayList<>(data.autoBuyItems);
    }

    @Override
    public void setAutoBuyStatus(UUID uuid, boolean status) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        data.autoBuy = status;
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public boolean getAutoBuyStatus(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData());
        return data.autoBuy;
    }

    private void scheduleAsyncUpdate(UUID uuid, PlayerData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String itemsStr = data.autoBuyItems.stream().collect(Collectors.joining(","));
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, data.score);
                ps.setBoolean(3, data.autoBuy);
                ps.setString(4, itemsStr);
                ps.setInt(5, data.score);
                ps.setBoolean(6, data.autoBuy);
                ps.setString(7, itemsStr);
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Ошибка обновления игрока " + uuid + ": " + e.getMessage());
            }
        });
    }
}