package net.jthomas.copperwarfare.entity.ai;

import net.jthomas.copperwarfare.Copperwarfare;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.animal.golem.CopperGolem;

import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.frog.Frog;

public class FactionAggressionBehavior extends Behavior<CopperGolem> {

    // A small counter to stop the logger from spamming your console 20 times a second
    private int logCooldown = 0;

    public FactionAggressionBehavior() {
        // The conditions required for this task to even be considered.
        // We only want to find a target if we don't already have one,
        // and if there are actually entities visible around us.
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem golem) {
        // Only run this logic if the golem has been tuned to aggressive mode
        // Safely cast to your custom entity to access your custom methods
        if (golem instanceof OverhauledCopperGolem overhauled) {

//            // Debugging the scan tick
//            if (logCooldown-- <= 0) {
//                Copperwarfare.LOGGER.info("[AI] Scanning... Aggressive Mode: " + overhauled.isAggressiveFactionMode());
//                logCooldown = 40; // Log roughly every 2 seconds
//            }

            return overhauled.isAggressiveFactionMode();
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CopperGolem golem, long gameTime) {
        if (golem instanceof OverhauledCopperGolem overhauled) {
            return overhauled.isAggressiveFactionMode();
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, CopperGolem golem, long gameTime) {
        // Double-check the cast here as well just to be safe
        if (!(golem instanceof OverhauledCopperGolem overhauled)) return;

        Copperwarfare.LOGGER.info("[AI] Aggression start triggered! Looking for targets...");

        // Grab the cached list of visible entities from the brain's memory
        Optional<NearestVisibleLivingEntities> visibleEntities =
                overhauled.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

        visibleEntities.ifPresent(entities -> {
            // Insert your custom faction logic here
            // For example, attack anything that isn't a fellow Copper Golem:
            Optional<LivingEntity> potentialTarget = entities.findClosest(entity -> {
                return entity.getType() == Copperwarfare.OVERHAULED_COPPER_GOLEM;
                // return entity.getType() != EntityType.COPPER_GOLEM;
            });

            // If we found a valid enemy, lock them into the attack target memory
            potentialTarget.ifPresent(enemy -> {
                Copperwarfare.LOGGER.info("[AI] Target acquired: " + enemy.getName().getString());
                overhauled.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, enemy);
            });
        });
    }
}