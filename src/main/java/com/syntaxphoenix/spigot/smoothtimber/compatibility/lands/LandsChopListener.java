package com.syntaxphoenix.spigot.smoothtimber.compatibility.lands;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.syntaxphoenix.spigot.smoothtimber.event.AsyncPlayerChopTreeEvent;
import com.syntaxphoenix.spigot.smoothtimber.event.reason.DefaultReason;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.role.enums.RoleSetting;

public final class LandsChopListener implements Listener {

    private final Lands addon;

    protected LandsChopListener(Lands addon) {
        this.addon = addon;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChopEvent(AsyncPlayerChopTreeEvent event) {
        LandsIntegration integration = addon.getIntegration();
        for (Location location : event.getBlockLocations()) {
            Area area = integration.getAreaByLoc(location);
            if (area == null) {
                continue;
            }
            if (!area.canSetting(event.getPlayer(), RoleSetting.BLOCK_BREAK, false)) {
                event.setCancelled(true);
                event.setReason(DefaultReason.LANDS);
                return;
            }
        }
    }
}
