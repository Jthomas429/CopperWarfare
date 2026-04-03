package net.jthomas.copperwarfare.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.jthomas.copperwarfare.Copperwarfare;
import net.jthomas.copperwarfare.ModSensors;
import net.jthomas.copperwarfare.entity.ai.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.jthomas.copperwarfare.Copperwarfare.MOD_ID;

public class OverhauledCopperGolem extends CopperGolem implements PolymerEntity {

    // Custom variable for your tuning rod state
    private boolean isAggressiveFactionMode = false;
    // Custom variable for breeding traits
    private double inheritedSpeedModifier = 1.0;
    // For the sensor logic we discussed
    private boolean isTunerNearby = false;
    // Golem Traits
    private GolemTraits currentTraits = new GolemTraits(1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    // logger if needed
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



    public OverhauledCopperGolem(EntityType<? extends CopperGolem> entityType, Level level) {
        super(entityType, level);
        this.setWeatherState(WeatheringCopper.WeatherState.UNAFFECTED);
    }


    // -------- ATTRIBUTES --------
    public static AttributeSupplier.Builder createOverhauledAttributes() {
        return CopperGolem.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D) // Baseline damage
                .add(Attributes.SCALE, 1.0D);        // Baseline size
    }

    public void applyTraits(GolemTraits traits) {
        this.currentTraits = traits;

        // Apply Speed (Vanilla base mob speed is usually around 0.25D)
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D * traits.speed());
        }

        // Apply Health (Vanilla golem health is likely 15.0D or 30.0D)
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15.0D * traits.hp());
            this.setHealth(this.getMaxHealth()); // Heal them so they don't look hurt when max HP increases
        }

        // Apply Strength
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D * traits.strength());
        }

        // Apply Size/Scale (This natively scales hitboxes AND visually scales the model!)
        if (this.getAttribute(Attributes.SCALE) != null) {
            this.getAttribute(Attributes.SCALE).setBaseValue(traits.size());
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);

        // This uses your Codec to "store" the whole record under one key
        valueOutput.store("golem_traits", GolemTraits.CODEC, this.currentTraits);
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);

        // Read the record back and immediately apply it to refresh attributes
        valueInput.read("golem_traits", GolemTraits.CODEC).ifPresent(this::applyTraits);
    }

    // A quick getter if your AI needs to read the traits
    public GolemTraits getTraits() {
        return this.currentTraits;
    }

    @Override
    protected Brain.@NonNull Provider<CopperGolem> brainProvider() {
        // Define exactly which memories your custom golem needs to function
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.LOOK_TARGET, // Needed for looking around
                        MemoryModuleType.PATH,        // Needed for pathfinding
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, // Needed for pathfinding timeout
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryModuleType.HURT_BY_ENTITY // Needed for retaliation attack
                ),
                ImmutableList.of(
                        SensorType.NEAREST_PLAYERS,
                        SensorType.NEAREST_LIVING_ENTITIES,
                        SensorType.HURT_BY,
                        ModSensors.TUNING_ROD_SENSOR
                )
        );
    }


    @Override
    protected @NonNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<CopperGolem> brain = this.brainProvider().makeBrain(dynamic);
        this.initCustomBrain(brain);
        return brain;
    }

    private void initCustomBrain(Brain<CopperGolem> brain) {
        // Core activities (movement, looking)
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new FactionAggressionBehavior()
        ));

        brain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, new GolemRetaliationBehavior()),
                Pair.of(1, new GolemBreedBehavior()),
                Pair.of(2, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(40, 80))),
                Pair.of(3, new GolemWanderBehavior(0.8f)),
                Pair.of(4, new DoNothing(30, 60))
        ));

        brain.addActivityWithConditions(Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new GolemRetaliationBehavior()),
                        Pair.of(1, StopAttackingIfTargetInvalid.create()),
                        Pair.of(2, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2f)),
                        Pair.of(3, new GolemMeleeAttackBehavior())
                ),
                // FIGHT is only eligible when we actually have a target
                ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
        );

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }


    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
//        // Temporary diagnostics — add this block
//        if (this.tickCount % 40 == 0) {
//            boolean hasWalkTarget = this.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isPresent();
//            boolean hasAttackTarget = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
//            Copperwarfare.LOGGER.info("[AI] Active: {} | WALK_TARGET: {} | ATTACK_TARGET: {}",
//                    this.getBrain().getActiveActivities(), hasWalkTarget, hasAttackTarget);
//        }

        ProfilerFiller profilerFiller = Profiler.get();

        profilerFiller.push("overhauledCopperGolemBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.pop();

        profilerFiller.push("copperGolemActivityUpdate");
        this.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
//        CopperGolemAi.updateActivity(this);
        profilerFiller.pop();

        //super.customServerAiStep(serverLevel);
    }

    // --- Polymer Implementation ---
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.COPPER_GOLEM;
    }

    @Override
    public void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {

        // SAFEGUARD: Remove existing vanilla attributes so we don't send conflicting duplicates
        // If the client receives two Speed values, it often ignores ours and defaults to 0.0
        data.removeIf(snapshot ->
                snapshot.attribute() == Attributes.SCALE ||
                        snapshot.attribute() == Attributes.MOVEMENT_SPEED ||
                        snapshot.attribute() == Attributes.MAX_HEALTH ||
                        snapshot.attribute() == Attributes.ATTACK_DAMAGE
        );

        // Add our genetically forged traits
        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.SCALE, this.getTraits().size(), List.of()));
        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MOVEMENT_SPEED, 0.25D * this.getTraits().speed(), List.of()));
        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MAX_HEALTH, 15.0D * this.getTraits().hp(), List.of()));
        data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.ATTACK_DAMAGE, 3.0D * this.getTraits().strength(), List.of()));

        PolymerEntity.super.modifyRawEntityAttributeData(data, player, initial);
    }


    // ----- FACTION AGGRO -----
    public boolean isAggressiveFactionMode() {
        return this.isAggressiveFactionMode;
    }

    // The setter you asked for
    public void setTunerNearby(boolean nearby) {
        this.isTunerNearby = nearby;
    }

    // Setter for the click logic
    public void setAggressiveFactionMode(boolean aggressive) {
        this.isAggressiveFactionMode = aggressive;

        // Server-side visual feedback: Play a sound so the player knows it worked!
        if (!this.level().isClientSide()) {
            float pitch = aggressive ? 1.5f : 0.5f;
            this.playSound(SoundEvents.COPPER_GOLEM_OXIDIZED_STEP, 1.0f, pitch);

            // Optional: Send a status message to the player (standard vanilla packet)
            // This works perfectly on Bedrock via Geyser.
        }
    }


    // ---- BREEDING MECHANICS ----
    private int inLoveTime = 0;

    // Getter and Setter
    public boolean isInLove() {
        return this.inLoveTime > 0;
    }

    public void setInLove(Player player) {
        this.inLoveTime = 600; // Lasts for 30 seconds (20 ticks * 30)
        //this.level().broadcastEntityEvent(this, (byte) 18); // Spawns vanilla heart particles!
        ServerLevel server = (ServerLevel) this.level();
        server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0, this.random.nextDouble() - 0.5, 0.1, this.random.nextDouble() - 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inLoveTime > 0) {
            this.inLoveTime--;
        }
    }


    public void spawnOffspring(ServerLevel level, OverhauledCopperGolem partner) {

        OverhauledCopperGolem baby = Copperwarfare.OVERHAULED_COPPER_GOLEM.create(level, EntitySpawnReason.BREEDING);
        if (baby == null) return;

        // 1. Forge the traits using your math class
        GolemTraits babyTraits = GolemGenetics.forgeOffspringTraits(this.getTraits(), partner.getTraits(), this.random);

        // 2. Apply them directly to the baby! (This changes its speed, health, damage, and size)
        baby.applyTraits(babyTraits);

        // Optional: If you want babies to start out at half of their genetic size until they "grow up"
        // baby.getAttribute(Attributes.SCALE).setBaseValue(babyTraits.size() * 0.5f);

        // 3. Setup and Spawn
        baby.setPos(this.getX() + this.random.nextDouble() - 0.5, this.getY(), this.getZ() + this.random.nextDouble() - 0.5);
        level.addFreshEntityWithPassengers(baby);

        // 4. Reset parents
        this.inLoveTime = 0;
        partner.inLoveTime = 0;

        // Optional: Spawn XP orb like vanilla breeding
        level.addFreshEntity(new ExperienceOrb(level, this.getX(), this.getY(), this.getZ(), random.nextInt(7) + 1));

        // Audio feedback is crucial!
        //this.playSound(SoundEvents.ANVIL_USE, 0.8F, 1.5F); // High-pitched anvil ding
        this.playSound(SoundEvents.COPPER_GOLEM_SPAWN);
    }


    @Override
    public @NonNull InteractionResult mobInteract(Player player, @NonNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Check if they are holding your Tuning Rod
        // (Replace Items.LIGHTNING_ROD with your actual custom item if you have one)
        if (itemStack.is(Items.LIGHTNING_ROD)) {
            if (!this.level().isClientSide()) {
                // Toggle the mode
                this.setAggressiveFactionMode(!this.isAggressiveFactionMode());

                // Visual indicator for the player
                String mode = this.isAggressiveFactionMode() ? "Aggressive" : "Passive";
                player.displayClientMessage(Component.literal("Copper Golem Mode: " + mode), true);

                // Swing the player's arm for feedback
                player.swing(hand);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        // BREEDING (Copper ingot)
        if (itemStack.is(Items.COPPER_INGOT) && !this.isInLove()) {
            if (!this.level().isClientSide()) {
                this.setInLove(player);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        //  --- THE STAT SCANNER (Sneak + Empty Hand)  ---
        if (player.isShiftKeyDown() && itemStack.isEmpty()) {
            if (!this.level().isClientSide()) {
                GolemTraits traits = this.getTraits();
                String modeName = this.isAggressiveFactionMode() ? "Aggressive" : "Passive";

//                // Send to the player's action bar (the 'true' boolean means it goes to the action bar)
//                player.displayClientMessage(Component.literal(scanResult), true);

                Display.TextDisplay hologram = EntityType.TEXT_DISPLAY.create(this.level(), EntitySpawnReason.SPAWN_ITEM_USE);
                if (hologram != null) {
                    hologram.setPos(this.getX(), this.getY() + this.getBbHeight() + 0.5D, this.getZ());
                    //hologram.setText(Component.literal("Speed: " + traits.speed() + "\nSize: " + traits.size()));

//                  // Format the text with some nice colors
//                  String text = String.format("§6Mode: §f%s\n§eSpeed: §f%.2f\n§aSize: §f%.2f\n§cHP: §f%.1f",
//                            modeName, traits.speed(), traits.size(), traits.hp());
                    // Format the string nicely
                    String text = String.format(
//                            "⚙ Mode: %s | ⚔ Strength: %.2f | ✈ Speed: %.2f | ⚡ AtkSpd: %.2f | 🗜 Size: %.2f | ❤ HP: %.1f | DPS: %.2f",
//                            modeName, traits.strength(), traits.speed(), traits.atkSpeed(), traits.size(), traits.hp(), traits.atkSpeed()*traits.strength()*3);
                    "⚔ Strength: %.2f | ✈ Speed: %.2f | ⚡ AtkSpd: %.2f | ❤ HP: %.1f | DPS: %.2f | TTK: %.2f | Combat: %.2f",
                            traits.strength(), traits.speed(), traits.atkSpeed(), traits.hp(),
                            traits.atkSpeed()*traits.strength()*3, // Damage per second
                            traits.hp()*15/3, // Seconds to live against default DPS
                            traits.atkSpeed()*traits.strength()*3*traits.hp()*15/3); // Combat Rating (Total Damage Dealt)
                    hologram.setText(Component.literal(text));

                    // Make it face the player
                    hologram.setBillboardConstraints(Display.TextDisplay.BillboardConstraints.CENTER);

                    // Set a nice background color (ARGB)
                    //hologram.setBackgroundColor(0x80000000); // Semi-transparent black (50% transparent)
                    hologram.setBackgroundColor(0xBF000000); // Lightly-transparent black (25% transparent)


                    // Spawn it in the world
                    this.level().addFreshEntity(hologram);

                    // Tell our manager to delete it after 100 ticks (5 seconds)
                    HologramManager.addHologram(hologram, 100);
                }

//                // Play a scanning sound like a spyglass or amethyst chime
//                golem.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);

                // Play a satisfying mechanical scan sound
                this.playSound(net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.2F);
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        return super.mobInteract(player, hand);
    }

}