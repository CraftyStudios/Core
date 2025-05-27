package dev.crafty.core.bridge.holograms.decentholograms;

import dev.crafty.core.bridge.holograms.Hologram;
import dev.crafty.core.bridge.holograms.HologramBridge;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import org.bukkit.Location;

import java.util.List;

public class DecentHologramsBridge implements HologramBridge {
    @Override
    public Hologram createHologram(Location location, String name, List<String> lines) {
        return new Hologram() {
            eu.decentsoftware.holograms.api.holograms.Hologram dhHolo;

            @Override
            public void show() {
                dhHolo = DHAPI.createHologram(name, location);
                for (String line : lines) {
                    DHAPI.addHologramLine(dhHolo, line);
                }
            }

            @Override
            public void hide() {
                DHAPI.removeHologram(dhHolo.getName());
            }

            @Override
            public void setLine(int line, String content) {
                HologramLine holoLine = DHAPI.getHologramLine(DHAPI.getHologramPage(dhHolo, 1), 1);

                if (holoLine != null) {
                    holoLine.setText(content);
                }
            }

            @Override
            public void setLocation(Location location) {
                if (dhHolo != null) {
                    dhHolo.setLocation(location);
                }
            }

            @Override
            public Location getLocation() {
                return dhHolo != null ? dhHolo.getLocation() : null;
            }
        };
    }

    @Override
    public String getName() {
        return "DecentHolograms";
    }

    @Override
    public boolean canRegister() {
        return pluginEnabled("DecentHolograms");
    }
}
