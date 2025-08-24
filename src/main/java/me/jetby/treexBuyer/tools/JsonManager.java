package me.jetby.treexBuyer.tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;


public class JsonManager {
    private final File file;
    private final Gson gson;
    private Map<String, Object> data;

    public JsonManager(File file) {
        this.file = file;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.data = new HashMap<>();
        load();
    }

    public void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            data = gson.fromJson(reader, type);
            if (data == null) data = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
        save();
    }

    public void setString(String key, String value) {
        data.put(key, value);
        save();
    }

    public String getString(String key, String def) {
        Object val = data.get(key);
        return val instanceof String ? (String) val : def;
    }

    public void setInt(String key, int value) {
        data.put(key, value);
        save();
    }

    public int getInt(String key, int def) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return def;
    }

    public void setBoolean(String key, boolean value) {
        data.put(key, value);
        save();
    }

    public boolean getBoolean(String key, boolean def) {
        Object val = data.get(key);
        return val instanceof Boolean ? (Boolean) val : def;
    }

    public void setDouble(String key, double value) {
        data.put(key, value);
        save();
    }

    public double getDouble(String key, double def) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return def;
    }

    public void setStringList(String key, List<String> list) {
        data.put(key, list);
        save();
    }
    public void set(String path, Object value) {
        data.put(path, value);
        save();
    }

    public Object get(String path) {
        return data.get(path);
    }

    public List<String> getStringList(String key) {
        Object val = data.get(key);
        if (val instanceof List<?>) {
            List<String> result = new ArrayList<>();
            for (Object o : (List<?>) val) {
                if (o instanceof String) result.add((String) o);
                else result.add(String.valueOf(o));
            }
            return result;
        }
        return new ArrayList<>();
    }

    public void setMap(String key, Map<String, Object> map) {
        data.put(key, map);
        save();
    }

    public Map<String, Object> getMap(String key) {
        Object val = data.get(key);
        if (val instanceof Map<?, ?>) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) val).entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return new HashMap<>();
    }
}