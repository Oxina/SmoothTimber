package com.syntaxphoenix.spigot.smoothtimber.compatibility.logblock;

import com.syntaxphoenix.spigot.smoothtimber.SmoothTimber;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.MaterialConverter;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.config.WorldConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used for accessing the logblock database, because the api queries to much not needed information,
 * which slow things down enormously.
 */
public class LogBlockDatabaseAccessor {

    private static final Set<Material> EMPTY_MATERIALS = Stream.of(
            Material.AIR, Material.getMaterial("CAVE_AIR"), Material.getMaterial("VOID_AIR")
    ).filter(Objects::nonNull).collect(Collectors.toSet());

    private final LogBlock logBlock;

    public LogBlockDatabaseAccessor(LogBlock logBlock) {
        this.logBlock = logBlock;
    }

    public boolean isPlayerPlaced(Block block) {
        String tableName = getTableName(block.getWorld());
        if (tableName == null)
            return false;

        try (Connection connection = logBlock.getConnection();
             PreparedStatement statement = connection.prepareStatement(constructQuery(tableName))) {
            statement.setInt(1, block.getX());
            statement.setInt(2, block.getY());
            statement.setInt(3, block.getZ());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next())
                    return false;
                if (resultSet.getInt("playerid") < 0)
                    return false;
                Material type = MaterialConverter.getMaterial(resultSet.getInt("type"));
                if (type.name().endsWith("SAPLING"))
                    return false;
                return !EMPTY_MATERIALS.contains(type);
            }
        } catch (SQLException throwables) {
            SmoothTimber.get().getLogger().severe("Failed to connect to LogBlock database");
            throwables.printStackTrace();
        }
        return true;
    }

    private String constructQuery(String tableName) {
        return "SELECT playerid, type FROM `" + tableName + "` WHERE x = ? AND y = ? AND z = ? " +
                "ORDER BY id DESC LIMIT 1";
    }

    /**
     * Returns the name of the block table for the provided {@link World}.
     *
     * @param world The world where the change detection should trigger.
     * @return The table-name or null, if the {@link World} is no LogBlock world.
     */
    private String getTableName(World world) {
        WorldConfig config = Config.getWorldConfig(world);
        if (config == null)
            return null;
        return config.table + "-blocks";
    }

}
