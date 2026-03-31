package net.jthomas.copperwarfare.entity.ai;

import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import org.jspecify.annotations.NonNull;

public class TuningRodSensor extends Sensor<CopperGolem> {

    // How often the sensor runs. 20 ticks = 1 second.
    // Scanning for players is slightly expensive, so we don't need to do it every single tick.
    private static final int SCAN_RATE = 10;

    public TuningRodSensor() {
        super(SCAN_RATE);
    }

    @Override
    public @NonNull Set<MemoryModuleType<?>> requires() {
        // We aren't strictly writing to a vanilla memory module here,
        // we are just updating our custom entity's state directly.
        return ImmutableSet.of();
    }

    @Override
    protected void doTick(@NonNull ServerLevel level, CopperGolem golem) {
        // 1. Cast to your custom entity so we can update your custom variables
        if (!(golem instanceof OverhauledCopperGolem overhauled)) return;

        // 2. Define the scanning area (e.g., an 8-block radius)
        List<Player> nearbyPlayers = level.getEntitiesOfClass(
                Player.class,
                golem.getBoundingBox().inflate(8.0D)
        );

        boolean isTunerNearby = false;

        // 3. Check if any of those players are holding the tuning rod
        for (Player player : nearbyPlayers) {
            // Check main hand and offhand
            if (player.getMainHandItem().is(Items.LIGHTNING_ROD) ||
                    player.getOffhandItem().is(Items.LIGHTNING_ROD)) {
                isTunerNearby = true;
                break; // We found one, no need to keep checking
            }
        }

        // 4. Update the golem's internal logic.
        // You'll need to create this setter method in your OverhauledCopperGolem class.
        overhauled.setTunerNearby(isTunerNearby);
    }
}