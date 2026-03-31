package net.jthomas.copperwarfare.entity;

import net.minecraft.world.entity.Display.TextDisplay;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HologramManager {
    // Stores the hologram entity and how many ticks it has left to live
    private static final Map<TextDisplay, Integer> activeHolograms = new HashMap<>();

    public static void addHologram(TextDisplay display, int lifespanTicks) {
        activeHolograms.put(display, lifespanTicks);
    }

    public static void initialize() {
        // Register this to run at the end of every server tick (20 times a second)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<TextDisplay, Integer>> iterator = activeHolograms.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<TextDisplay, Integer> entry = iterator.next();
                TextDisplay display = entry.getKey();
                int ticksLeft = entry.getValue() - 1;

                // If time is up, or the entity was killed by something else, clean it up
                if (ticksLeft <= 0 || display.isRemoved()) {
                    display.discard();
                    iterator.remove();
                } else {
                    entry.setValue(ticksLeft);
                }
            }
        });
    }
}