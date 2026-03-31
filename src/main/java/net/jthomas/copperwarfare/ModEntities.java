package net.jthomas.copperwarfare;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.jthomas.copperwarfare.entity.GolemTraits;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.entity.ai.TuningRodSensor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

import static net.jthomas.copperwarfare.Copperwarfare.MOD_ID;

public class ModEntities {

//    public static final ResourceKey<EntityType<?>> OVERHAULED_COPPER_GOLEM_KEY =
//            ResourceKey.create(Registries.ENTITY_TYPE,
//                    Identifier.fromNamespaceAndPath(MOD_ID, "overhauled_copper_golem"));
//
//    public static final EntityType<OverhauledCopperGolem> OVERHAULED_COPPER_GOLEM =
//            Registry.register(BuiltInRegistries.ENTITY_TYPE,
//                    OVERHAULED_COPPER_GOLEM_KEY,
//                    EntityType.Builder.of(OverhauledCopperGolem::new, MobCategory.CREATURE)
//                            .build(OVERHAULED_COPPER_GOLEM_KEY));

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void register() {

//    PolymerEntityUtils.registerOverlay(OVERHAULED_COPPER_GOLEM, (entity) -> new PolymerEntity() {
//            @Override
//            public EntityType<?> getPolymerEntityType(PacketContext context) {
//                return EntityType.COPPER_GOLEM;
//            }
//    });


//        PolymerEntityUtils.registerOverlay(OVERHAULED_COPPER_GOLEM, (entity) -> new PolymerEntity() {
//            @Override
//            public EntityType<?> getPolymerEntityType(PacketContext context) {
//                return EntityType.COPPER_GOLEM;
//            }

//            @Override
//            public void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {
//                // We MUST cast the entity to access your custom traits
//                if (entity instanceof OverhauledCopperGolem golem) {
//                    GolemTraits traits = golem.getTraits();
//
//                    // 1. Sync Scale
//                    data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.SCALE, traits.size(), List.of()));
//
//                    // 2. Sync Speed (This fixes the "not moving" bug!)
//                    // We use the same math as your applyTraits method: 0.25 * multiplier
//                    data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MOVEMENT_SPEED, 0.25D * traits.speed(), List.of()));
//
//                    // 3. Sync Max Health (So the health bar looks correct)
//                    data.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(Attributes.MAX_HEALTH, 15.0D * traits.hp(), List.of()));
//                }
//
//                PolymerEntity.super.modifyRawEntityAttributeData(data, player, initial);
//            }


    }
}
