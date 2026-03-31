package net.jthomas.copperwarfare.item;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.entity.poly.PolymerAutoItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class TuningRodItem extends Item implements PolymerAutoItem {

    public TuningRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.LIGHTNING_ROD;
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        // Since we are strictly building server-side mechanics for Polymer compatibility,
        // we lock this behind the server check.
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player == null) return InteractionResult.PASS;
            triggerAoE(level, player, true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

//    // LEFT CLICK (on a block): Set nearby golems to PASSIVE
//    @Override
//    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
//        if (!level.isClientSide()) {
//            triggerAoE(level, player, false);
//        }
//        // Return false so we don't actually break the block
//        return false;
//    }

    public void triggerAoE(Level level, Player player, boolean makeAggressive) {
        double radius = 16.0D;

        AABB area = player.getBoundingBox().inflate(radius);
        List<OverhauledCopperGolem> nearbyGolems = level.getEntitiesOfClass(OverhauledCopperGolem.class, area);

        for (OverhauledCopperGolem golem : nearbyGolems) {
            golem.setAggressiveFactionMode(makeAggressive);

            // Play a small "confirm" effect on each golem
            ((ServerLevel)level).sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    golem.getX(), golem.getY() + 1, golem.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);
        }

        // Sound Feedback
        float pitch = makeAggressive ? 1.5f : 0.8f;
        level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0f, pitch);

        // Visual Wave Feedback
        spawnParticleRing((ServerLevel) level, player, radius);

        // Cooldown to prevent spam
        // player.getCooldowns().addCooldown(player.getActiveItem(), 10);
        player.getCooldowns().addCooldown(this.getDefaultInstance(), 10);
    }


    void spawnParticleRing(ServerLevel level, Player player, double radius) {
        // Create a ring of particles expanding outward
        for (int i = 0; i < 360; i += 10) {
            double radians = Math.toRadians(i);
            double x = player.getX() + (Math.cos(radians) * radius);
            double z = player.getZ() + (Math.sin(radians) * radius);
            level.sendParticles(ParticleTypes.CRIT, x, player.getY() + 0.1, z, 1, 0, 0, 0, 0);
        }
    }
}



