package net.jthomas.copperwarfare;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.jthomas.copperwarfare.entity.HologramManager;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.entity.ai.TuningRodSensor;
import net.jthomas.copperwarfare.item.TuningRodItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Copperwarfare implements ModInitializer {

    public static final String MOD_ID = "copperwarfare";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<EntityType<?>> OVERHAULED_COPPER_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, "overhauled_copper_golem"));

    public static final EntityType<OverhauledCopperGolem> OVERHAULED_COPPER_GOLEM =
            Registry.register(BuiltInRegistries.ENTITY_TYPE,
                    OVERHAULED_COPPER_GOLEM_KEY,
                    EntityType.Builder.of(OverhauledCopperGolem::new, MobCategory.CREATURE)
                            .build(OVERHAULED_COPPER_GOLEM_KEY));



    @Override
    public void onInitialize() {
        ModAttachments.initialize();
        ModEntities.register();
        ModSensors.register();
        ModCommands.register();
        ModItems.registerModItems();
        HologramManager.initialize();

        LOGGER.info("Initialized COPPERWARFARE.JAVA");

        FabricDefaultAttributeRegistry.register(OVERHAULED_COPPER_GOLEM, OverhauledCopperGolem.createOverhauledAttributes());

        PolymerEntityUtils.registerType(OVERHAULED_COPPER_GOLEM);

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack held = player.getItemInHand(hand);

            if (held.getItem() instanceof TuningRodItem rod) {
                if (!world.isClientSide()) {
                    rod.triggerAoE(world, player, false); // Left click = passive
                }
                // PASS still lets the block interaction complete normally,
                // SUCCESS_SERVER cancels the attack swing clientside
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        });


//        public static <T extends Entity> EntityType<T> registerEntity(String entity, EntityType.Builder<T> v) {
//            return register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", entity), v.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", entity))));
//        }

        //Registry.register(BuiltInRegistries.SENSOR_TYPE, Identifier.withDefaultNamespace("tuning_rod_sensor"), new SensorType(TuningRodSensor::new));
    }
}
