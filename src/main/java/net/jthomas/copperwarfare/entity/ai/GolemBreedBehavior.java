package net.jthomas.copperwarfare.entity.ai;

import net.jthomas.copperwarfare.Copperwarfare;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.CopperGolem;

public class GolemBreedBehavior extends Behavior<CopperGolem> {

    private int logCooldown = 0;
    private long lastScanTime = 0;
    private OverhauledCopperGolem targetPartner = null;

    public GolemBreedBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED // We will write to this
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem golem) {
        // Only run if it's our overhauled class and it's in love
        return golem instanceof OverhauledCopperGolem overhauled && overhauled.isInLove();
    }

    @Override
    protected void start(ServerLevel level, CopperGolem golem, long gameTime) {
        this.targetPartner = null;
    }

    @Override
    protected void tick(ServerLevel level, CopperGolem golem, long gameTime) {
        if (!(golem instanceof OverhauledCopperGolem overhauled)) return;

        // 1. Find a partner if we don't have one
        if (this.targetPartner == null || !this.targetPartner.isAlive() || !this.targetPartner.isInLove()) {
            Optional<OverhauledCopperGolem> potentialPartner = overhauled.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    .flatMap(entities -> entities.findClosest(e ->
                            e instanceof OverhauledCopperGolem other && other.isInLove() && other != overhauled
                    ))
                    .map(e -> (OverhauledCopperGolem) e);

            potentialPartner.ifPresent(partner -> this.targetPartner = partner);

//            if (this.targetPartner == null || !this.targetPartner.isAlive() || !this.targetPartner.isInLove()) {
//                overhauled.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(entities -> {
//                    entities.findClosest(e -> e instanceof OverhauledCopperGolem other && other.isInLove() && other != overhauled)
//                            .ifPresent(p -> this.targetPartner = (OverhauledCopperGolem) p);
//                });
//            }

        }

        // 2. Move towards partner and breed
        if (this.targetPartner != null) {
            //double distance = overhauled.distanceToSqr(this.targetPartner);

            double reach = 1 + (overhauled.getBbWidth() / 2.0) + (this.targetPartner.getBbWidth() / 2.0);
            double distance = overhauled.distanceToSqr(this.targetPartner);

            if (distance < (reach * reach)) { // Close enough to breed!
                overhauled.spawnOffspring(level, this.targetPartner);
                overhauled.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                this.targetPartner = null;
            } else {
                // Keep walking toward them
                overhauled.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPartner, 1.0f, 1));
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CopperGolem golem, long gameTime) {
        return golem instanceof OverhauledCopperGolem overhauled && overhauled.isInLove();
    }
}