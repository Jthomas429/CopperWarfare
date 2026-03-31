package net.jthomas.copperwarfare.item;

import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.entity.poly.PolymerAutoItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class TuningRodItem_Backup extends Item implements PolymerAutoItem {

    public TuningRodItem_Backup(Properties properties) {
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

            // 1. Define the radius (16 blocks in all directions from the clicked block)
            double radius = 16.0D;
            Vec3 hitPos = context.getClickLocation();
            AABB searchArea = new AABB(hitPos, hitPos).inflate(radius);

            // 2. Find all custom golems in that box
            List<OverhauledCopperGolem> nearbyGolems = level.getEntitiesOfClass(OverhauledCopperGolem.class, searchArea);
            boolean toggledAny = false;

            // 3. Issue the command to all of them
            for (OverhauledCopperGolem golem : nearbyGolems) {
                // Toggle their aggro state
                golem.setAggressiveFactionMode(!golem.isAggressiveFactionMode());
                toggledAny = true;

                // Visual feedback from the golem
                level.broadcastEntityEvent(golem, (byte) 14);
            }

            // 4. Feedback for the player
            if (toggledAny) {
                // Play a resounding bell/chime sound where the player struck the block
                level.playSound(null, context.getClickedPos(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);

                // 1-second cooldown
                player.getCooldowns().addCooldown(context.getItemInHand(), 20);
            }
        }

        // Return SUCCESS to let the game know the action was completed
        return InteractionResult.SUCCESS;
    }
}



