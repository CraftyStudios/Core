package dev.crafty.core.bridge.mobs.mythicmobs;

import dev.crafty.core.bridge.mobs.MobBridge;
import dev.crafty.core.geometry.Point3d;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class MythicMobsBridge implements MobBridge {

    @Override
    public Optional<Entity> spawnMob(String mobName, Point3d location, String world, int level) {
        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(mobName).orElse(null);
        Location spawnLocation = new Location(
                Bukkit.getWorld(world),
                location.x(),
                location.y(),
                location.z()
        );
        if (mob != null) {
            // spawns mob
            ActiveMob knight = mob.spawn(BukkitAdapter.adapt(spawnLocation),level);

            Entity entity = knight.getEntity().getBukkitEntity();

            return Optional.of(entity);
        }

        return Optional.empty();
    }

    @Override
    public String getName() {
        return "MythicMobs";
    }

    @Override
    public boolean canRegister() {
        return pluginEnabled("MythicMobs");
    }
}
