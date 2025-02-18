package com.syntaxphoenix.spigot.smoothtimber.compatibility.coreprotect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.syntaxphoenix.spigot.smoothtimber.version.manager.VersionChanger;

import net.coreprotect.CoreProtectAPI;

public class CoreCompat_v1_8_x implements CoreCompat {

    private final CoreProtectAPI api;
    private final VersionChanger changer;

    protected CoreCompat_v1_8_x(Plugin plugin, VersionChanger changer) {
        api = ((net.coreprotect.CoreProtect) plugin).getAPI();
        this.changer = changer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void logRemoval(String user, Location location, Block block) {
        api.logRemoval(user, location, block.getType(), changer.getData(block));
    }

}
