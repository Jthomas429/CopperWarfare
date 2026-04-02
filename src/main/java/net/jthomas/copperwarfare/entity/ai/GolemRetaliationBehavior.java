package net.jthomas.copperwarfare.entity.ai;

import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.golem.CopperGolem;

import java.util.Map;

public class GolemRetaliationBehavior extends Behavior<CopperGolem> {

    public GolemRetaliationBehavior() {
        super(Map.of(
                // This behavior ONLY runs when someone hurts us
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected void start(ServerLevel level, CopperGolem golem, long gameTime) {
        // 1. Find out who hit us
        LivingEntity attacker = golem.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get();

//        // Optional Faction Safety: Prevent them from attacking their own kind
//        if (attacker instanceof OverhauledCopperGolem) {
//            golem.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
//            return;
//        }

        // 2. Set the attacker as the new ATTACK_TARGET, overriding whatever they were doing
        golem.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);

        // 3. Clear the "hurt by" memory so this behavior shuts down until they are hit again
        golem.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);

        // 4. If they were standing around in Passive mode, snap them into Aggressive mode!
        if (golem instanceof OverhauledCopperGolem overhauled) {
            if (!overhauled.isAggressiveFactionMode()) {
                overhauled.setAggressiveFactionMode(true);
            }
        }
    }
}