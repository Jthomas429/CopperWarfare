package net.jthomas.copperwarfare.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class GolemWanderBehavior extends Behavior<CopperGolem> {

    private final float speedModifier;
    private static final int MIN_INTERVAL = 80;   // ticks between wander attempts
    private static final int MAX_INTERVAL = 160;
    private int nextWanderTick = 0;

    public GolemWanderBehavior(float speedModifier) {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem golem) {
        return golem.tickCount >= nextWanderTick;
    }

    @Override
    protected void start(ServerLevel level, CopperGolem golem, long gameTime) {
        // Pick a random nearby position
        Vec3 target = DefaultRandomPos.getPos(golem, 10, 7);
        if (target != null) {
            golem.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                    new WalkTarget(target, speedModifier, 1));
        }
        // Schedule next attempt regardless of whether we found a position
        nextWanderTick = golem.tickCount + MIN_INTERVAL
                + golem.getRandom().nextInt(MAX_INTERVAL - MIN_INTERVAL);
    }
}