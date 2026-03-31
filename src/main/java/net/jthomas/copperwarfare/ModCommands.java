package net.jthomas.copperwarfare;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jthomas.copperwarfare.entity.GolemTraits;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("golemstats").executes(context -> {
                var player = context.getSource().getPlayerOrException();

                // 1. Find what the player is looking at
                Vec3 eyePosition = player.getEyePosition();
                Vec3 lookVector = player.getViewVector(1.0F).scale(10.0D); // 10 block range
                Vec3 reachVector = eyePosition.add(lookVector);

                EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                        player, eyePosition, reachVector,
                        player.getBoundingBox().expandTowards(lookVector).inflate(1.0D),
                        e -> e instanceof OverhauledCopperGolem, 10.0D
                );

                if (hit != null && hit.getEntity() instanceof OverhauledCopperGolem golem) {
                    GolemTraits traits = golem.getTraits();

                    // 2. Format and send the message
                    player.sendSystemMessage(Component.literal("§6--- Golem Genetics ---"));
                    player.sendSystemMessage(Component.literal("§7Speed: §f" + String.format("%.2f", traits.speed()) + "x"));
                    player.sendSystemMessage(Component.literal("§7AtkSpeed: §f" + String.format("%.2f", traits.atkSpeed()) + "x"));
                    player.sendSystemMessage(Component.literal("§7Strength: §f" + String.format("%.2f", traits.strength()) + "x"));
                    player.sendSystemMessage(Component.literal("§7HP: §f" + String.format("%.2f", traits.hp()) + "x"));
                    player.sendSystemMessage(Component.literal("§7Size: §f" + String.format("%.2f", traits.size()) + "x"));
                    player.sendSystemMessage(Component.literal("§6----------------------"));
                    player.sendSystemMessage(Component.literal("§DDPS: §f" + String.format("%.2f", traits.atkSpeed()*traits.strength()*3) + "x"));
                    player.sendSystemMessage(Component.literal("§6----------------------"));

                    return 1;
                }

                player.sendSystemMessage(Component.literal("§cYou must be looking at an Overhauled Copper Golem!"));
                return 0;
            }));
        });
    }
}