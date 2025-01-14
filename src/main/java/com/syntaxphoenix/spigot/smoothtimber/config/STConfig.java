package com.syntaxphoenix.spigot.smoothtimber.config;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.syntaxphoenix.syntaxapi.reflection.Reflect;
import com.syntaxphoenix.syntaxapi.utils.java.Times;

public abstract class STConfig {

    private final Reflect migrationRef;

    protected long loaded = -1;

    protected File file;
    protected YamlConfiguration config = new YamlConfiguration();

    protected final int latestVersion;
    protected int version;

    public STConfig(File file, Class<? extends Migration> clazz, int latestVersion) {
        this.file = file;
        this.migrationRef = new Reflect(clazz);
        this.latestVersion = latestVersion;
        this.version = latestVersion;
    }

    /*
     * Getter
     */

    public final Reflect getMigrationRef() {
        return migrationRef;
    }

    public final int getLatestVersion() {
        return latestVersion;
    }

    public final int getVersion() {
        return version;
    }

    /*
     * Management
     */

    public <E> E check(String path, E input) {
        if (config.contains(path)) {
            E value = safeCast(input, get(path));
            if (value == input) {
                set(path, input);
                return input;
            }
            return value;
        }
        set(path, input);
        return input;
    }

    @SuppressWarnings("unchecked")
    public <N extends Number> N check(String path, N input) {
        if (config.contains(path)) {
            Number number = safeNumber(get(path));
            if (number == null) {
                set(path, input);
                return input;
            }
            return (N) NumberType.get(input.getClass(), number);
        }
        set(path, input);
        return input;
    }

    public <E> E get(String path, E input) {
        if (config.contains(path)) {
            return safeCast(input, get(path));
        }
        return input;
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String[] getKeys(String path) {
        return Optional.of(config).filter(config -> config.isConfigurationSection(path))
            .map(config -> config.getConfigurationSection(path).getKeys(false).toArray(new String[0])).orElseGet(() -> new String[0]);
    }

    public void set(String path, Object input) {
        config.set(path, input);
    }

    /*
     * IO
     */

    public final void reload() {
        load();

        if (loaded == -1) {
            ConfigTimer.TIMER.load(this);
            onSetup();
        }
        loaded = file.lastModified();

        if (file.exists()) {
            version = check("version", 1);
        }

        if (latestVersion > version) {
            MigrationContext context = new MigrationContext(config);
            while (latestVersion > version) {
                String method = "update" + version++;
                migrationRef.searchMethod(method, method, MigrationContext.class).execute(method, context);
            }
            file.delete();
            config = new YamlConfiguration();
            context.remove("version");
            config.set("version", version);
            for (Entry<String, Object> entry : context.getValues().entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        } else if (latestVersion < version) {
            backupAndClear();
        }

        onLoad();

        save();
        loaded = file.lastModified();
    }

    public final void unload() {
        onUnload();
        save();

        ConfigTimer.TIMER.unload(this);

        loaded = -1;
        config = new YamlConfiguration();
    }

    private final void backupAndClear() {
        String name = file.getName().replace(".yml", "") + "-" + Times.getDate("_") + "-backup-%count%.yml";
        int tries = 0;

        String parent = file.getParent();

        File backupFile = new File(parent, name.replace("%count%", tries + ""));
        while (backupFile.exists()) {
            backupFile = new File(parent, name.replace("%count%", (tries++) + ""));
        }

        try {
            config.save(backupFile);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = new YamlConfiguration();
    }

    private final void load() {
        if (!file.exists()) {
            return;
        }
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private final void save() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            }
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Type Handle
     */

    protected abstract String getSingleType();

    protected abstract String getMultipleType();

    /*
     * Handle
     */

    protected abstract void onSetup();

    protected abstract void onLoad();

    protected abstract void onUnload();

    /*
     * Utils
     */

    @SuppressWarnings("unchecked")
    protected <E> E safeCast(E sample, Object input) {
        return (input != null && sample.getClass().isAssignableFrom(input.getClass())) ? (E) input : sample;
    }

    protected Number safeNumber(Object input) {
        return (input != null && Number.class.isAssignableFrom(input.getClass())) ? (Number) input : null;
    }

}
