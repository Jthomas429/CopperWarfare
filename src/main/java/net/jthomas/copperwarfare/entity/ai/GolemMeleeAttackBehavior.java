package net.jthomas.copperwarfare.entity.ai;

import com.google.common.collect.ImmutableMap;
import net.jthomas.copperwarfare.Copperwarfare;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.animal.golem.CopperGolem;

import java.util.Map;
import java.util.Optional;

public class GolemMeleeAttackBehavior extends Behavior<CopperGolem> {

    private int tickCount = 0;
    protected int attackCooldown = 1; //default value just in case

    public GolemMeleeAttackBehavior() {//int i) {
        super(Map.of(
                //MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        //this.attackCooldown = i;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem golem) {
        // Only run if it's our overhauled class
        return golem instanceof OverhauledCopperGolem;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CopperGolem golem, long gameTime) {
        return golem.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
    }

    @Override
    protected void start(ServerLevel level, CopperGolem golem, long gameTime) {
        // Double-check the cast here as well just to be safe
        if (!(golem instanceof OverhauledCopperGolem overhauled)) return;
        attackCooldown = Math.round(20 / overhauled.getTraits().atkSpeed());
    }

    @Override
    protected void tick(ServerLevel level, CopperGolem owner, long gameTime) {
        tickCount++;
        if (!(owner instanceof OverhauledCopperGolem overhauled)) return;

        // Get target and distance to target
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        double reach = 1.8 + (overhauled.getBbWidth() / 2.0) + (target.getBbWidth() / 2.0);

        if (overhauled.distanceToSqr(target) < reach * reach) {

            Copperwarfare.LOGGER.info("[AI] ATTEMPTING DAMAGE...");

            if (tickCount % attackCooldown == 0) { // Attack once per second
                //float damage = overhauled.getTraits().strength() * 3.0f;
                float damage = (float) overhauled.getAttributeValue(Attributes.ATTACK_DAMAGE);
                boolean successfullyHit = target.hurtServer(
                        (ServerLevel) overhauled.level(),
                        overhauled.damageSources().mobAttack(overhauled),
                        damage);

                if (successfullyHit) {
                    tickCount = 0;
                    Copperwarfare.LOGGER.info("[AI] SUCCESSFUL HIT...");
                    // Apply standard vanilla knockback
                    if (target instanceof LivingEntity livingEnemy) {
                        float knockbackStrength = 0.5f;
                        livingEnemy.knockback(knockbackStrength,
                                Mth.sin(overhauled.getYRot() * ((float)Math.PI / 180F)),
                                -Mth.cos(overhauled.getYRot() * ((float)Math.PI / 180F))
                        );
                        Copperwarfare.LOGGER.info("[AI] AND KNOCKBACK...");
                    }

                    // Play an attack sound
                    overhauled.playSound(SoundEvents.COPPER_GOLEM_STEP, 1.0f, 0.5f);
                    Copperwarfare.LOGGER.info("[AI] AND SOUND...");

                    // Tell the clients to play the physical arm-swing animation!
                    overhauled.swing(InteractionHand.MAIN_HAND);
                    Copperwarfare.LOGGER.info("[AI] AND SWING 1...");
                    overhauled.level().broadcastEntityEvent(overhauled, (byte) 4);
                    Copperwarfare.LOGGER.info("[AI] AND SWING 2...");
                }
            }
        }
    }
}