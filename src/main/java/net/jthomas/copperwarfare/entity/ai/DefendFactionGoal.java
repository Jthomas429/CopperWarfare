//package net.jthomas.copperwarfare.entity.ai;
//
//import net.jthomas.copperwarfare.ModAttachments;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.entity.animal.golem.CopperGolem;
//import net.minecraft.world.entity.ai.memory.MemoryModuleType;
//import java.util.EnumSet;
//
//public class DefendFactionGoal extends Goal {
//    private final CopperGolem golem;
//
//    public DefendFactionGoal(CopperGolem golem) {
//        this.golem = golem;
//        // This tells the GoalSelector that this goal requires control over movement and looking
//        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
//    }
//
//    @Override
//    public boolean canUse() {
//        // Only run if the golem's aggro mode is set to Defend (1) or Aggressive (2)
//        byte mode = this.golem.getAttachedOrCreate(ModAttachments.AGGRO_MODE);
//        return mode > 0 && this.golem.getTarget() != null;
//    }
//
//    @Override
//    public void start() {
//        // When combat starts, instantly erase any current Brain tasks (like walking to a chest)
//        this.suppressVanillaBrain();
//    }
//
//    @Override
//    public void tick() {
//        // 1. Constantly suppress the Brain so it doesn't regain control mid-fight
//        this.suppressVanillaBrain();
//
//        // 2. Do your normal combat logic here
//        var target = this.golem.getTarget();
//        if (target != null) {
//            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
//            this.golem.getNavigation().moveTo(target, 1.25D); // Use speed from your traits later!
//
//            // TODO: Add attack cooldown and melee punch logic
//        }
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