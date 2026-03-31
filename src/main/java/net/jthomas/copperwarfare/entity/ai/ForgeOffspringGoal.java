//package net.jthomas.copperwarfare.entity.ai;
//
//import net.jthomas.copperwarfare.ModAttachments;
//import net.jthomas.copperwarfare.entity.GolemGenetics;
//import net.jthomas.copperwarfare.entity.GolemTraits;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.world.entity.EntitySpawnReason;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.entity.ai.memory.MemoryModuleType;
//import net.minecraft.world.entity.animal.golem.CopperGolem;
//
//import java.util.EnumSet;
//import java.util.List;
//
//
//public class ForgeOffspringGoal extends Goal {
//
//    private final CopperGolem golem;
//    private CopperGolem partner;
//    private int forgeTimer;
//
//    public ForgeOffspringGoal(CopperGolem golem) {
//        this.golem = golem;
//        // Require control over movement and look direction
//        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
//    }
//
//    @Override
//    public boolean canUse() {
//        // 1. If this golem hasn't been given a Copper Ingot, ignore this goal
//        if (!this.golem.getAttachedOrCreate(ModAttachments.FORGE_READY)) {
//            return false;
//        }
//
//        // 2. Scan an 8-block radius for another ready golem
//        List<CopperGolem> nearbyGolems = this.golem.level().getEntitiesOfClass(
//                CopperGolem.class,
//                this.golem.getBoundingBox().inflate(8.0D)
//        );
//
//        for (CopperGolem potentialPartner : nearbyGolems) {
//            // Make sure it's not looking at itself, and the partner is also ready
//            if (potentialPartner != this.golem && potentialPartner.getAttachedOrCreate(ModAttachments.FORGE_READY)) {
//                this.partner = potentialPartner;
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public boolean canContinueToUse() {
//        // Keep running as long as both are alive and still flagged as ready
//        return this.partner != null
//                && this.partner.isAlive()
//                && this.golem.getAttachedOrCreate(ModAttachments.FORGE_READY)
//                && this.partner.getAttachedOrCreate(ModAttachments.FORGE_READY);
//    }
//
//    @Override
//    public void start() {
//        this.forgeTimer = 0;
//
//        // 1. Constantly suppress the Brain so it doesn't regain control
//        this.suppressVanillaBrain();
//    }
//
//    @Override
//    public void tick() {
//        // 1. Constantly suppress the Brain so it doesn't regain control
//        this.suppressVanillaBrain();
//
//        // Stare deeply into each other's glowing redstone eyes
//        this.golem.getLookControl().setLookAt(this.partner, 10.0F, 10.0F);
//
//        // Walk toward each other
//        this.golem.getNavigation().moveTo(this.partner, 1.0D);
//
//        // If they are within ~2 blocks of each other, start "forging"
//        if (this.golem.distanceToSqr(this.partner) < 4.0D) {
//            this.forgeTimer++;
//
//            // Periodically throw some sparks/hearts to show progress
//            if (this.forgeTimer % 10 == 0) {
//                this.golem.level().broadcastEntityEvent(this.golem, (byte) 18);
//            }
//
//            // After 3 seconds (60 ticks) of touching, spawn the baby!
//            if (this.forgeTimer >= 60) {
//                this.forgeOffspring();
//            }
//        } else {
//            // If they get bumped away from each other, reset the timer
//            this.forgeTimer = 0;
//        }
//    }
//
//    private void forgeOffspring() {
//        if (!(this.golem.level() instanceof ServerLevel serverLevel)) return;
//
//        // 1. Instantiate the vanilla entity
//        CopperGolem baby = EntityType.COPPER_GOLEM.create(serverLevel, EntitySpawnReason.BREEDING);
//        if (baby == null) return;
//
//        // 2. Grab the parents' genes and run your custom math!
//        var traitsA = this.golem.getAttachedOrCreate(ModAttachments.GOLEM_TRAITS);
//        var traitsB = this.partner.getAttachedOrCreate(ModAttachments.GOLEM_TRAITS);
//        var babyTraits = GolemGenetics.forgeOffspringTraits(traitsA, traitsB, this.golem.getRandom());
//
//        // 3. Attach the mutated genes to the baby
//        baby.setAttached(ModAttachments.GOLEM_TRAITS, babyTraits);
//
//        // 4. Spawn it directly on top of the parents
//        baby.setPos(this.golem.getX(), this.golem.getY(), this.golem.getZ());
//        serverLevel.addFreshEntityWithPassengers(baby);
//
//        // 4b. Set baby golem size
//        golem.getAttribute(Attributes.SCALE).setBaseValue(babyTraits.size());
//
//        // 5. Revoke the parents' breeding privileges so they don't infinitely multiply
//        this.golem.setAttached(ModAttachments.FORGE_READY, false);
//        this.partner.setAttached(ModAttachments.FORGE_READY, false);
//
//        // 6. Audio feedback is crucial!
//        this.golem.playSound(SoundEvents.ANVIL_USE, 0.8F, 1.5F); // High-pitched anvil ding
//    }
//
//    private void suppressVanillaBrain() {
//        // Wiping these memories guarantees the vanilla item-sorting AI stays paralyzed
//        var brain = this.golem.getBrain();
//        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
//        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
//        brain.eraseMemory(MemoryModuleType.PATH);
//        brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
//        brain.eraseMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
//    }
//}
