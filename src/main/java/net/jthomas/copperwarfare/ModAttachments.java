package net.jthomas.copperwarfare;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.*;
import net.puffish.attributesmod.*;

import java.util.Optional;
import java.util.UUID;

public class ModAttachments {

//    // 1. THE RECORD
//    // Records are perfect here since they act as immutable data carriers.
//    public record GolemTraits(float speed, float strength, float hp, float size) {
//
//        // 2. THE CODEC
//        // This tells Minecraft/Fabric exactly how to serialize this record to NBT/JSON
//        // so it saves automatically to the chunk data when the server stops.
//        public static final Codec<GolemTraits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//                Codec.FLOAT.fieldOf("speed").forGetter(GolemTraits::speed),
//                Codec.FLOAT.fieldOf("strength").forGetter(GolemTraits::strength),
//                Codec.FLOAT.fieldOf("hp").forGetter(GolemTraits::hp),
//                Codec.FLOAT.fieldOf("size").forGetter(GolemTraits::size)
//        ).apply(instance, GolemTraits::new));
//    }

//    // 3. REGISTERING THE TRAITS ATTACHMENT
//    // We bind our GolemTraits record to an AttachmentType.
//    public static final AttachmentType<GolemTraits> GOLEM_TRAITS = AttachmentRegistry.create(
//            Identifier.fromNamespaceAndPath("coppergolems", "golem_traits"),
//            builder -> builder
//                    // If a vanilla golem spawns and doesn't have traits yet, it gets these baseline stats.
//                    .initializer(() -> new GolemTraits(1.0f, 1.0f, 1.0f, 1.0f))
//                    // Tell Fabric to persist this data across restarts using our Codec.
//                    .persistent(GolemTraits.CODEC)
//    );

//    // 4. REGISTERING THE AGGRO MODE ATTACHMENT
//    // 0 = Passive (Vanilla Sorting), 1 = Defend, 2 = Aggressive
//    public static final AttachmentType<Byte> AGGRO_MODE = AttachmentRegistry.create(
//            Identifier.fromNamespaceAndPath("coppergolems", "aggro_mode"),
//            builder -> builder
//                    .initializer(() -> (byte) 0)
//                    .persistent(Codec.BYTE)
//    );

    // 5. REGISTERING THE CREATOR UUID ATTACHMENT
    // Used so your custom AI knows who to follow or who NOT to punch.
    public static final AttachmentType<Optional<UUID>> CREATOR_UUID = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("coppergolems", "creator_uuid"),
            builder -> builder
                    .initializer(Optional::empty)
                    // optionalField makes it easy to handle golems that spawn naturally without a player creator.
                    .persistent(Codec.optionalField("uuid", UUIDUtil.CODEC, true).codec())
    );

//    // 6. REGISTERING THE FORGING MODE
//    // true = ready to breed
//    public static final AttachmentType<Boolean> FORGE_READY = AttachmentRegistry.create(
//            Identifier.fromNamespaceAndPath("coppergolems", "forge_ready"),
//            builder -> builder
//                    .initializer(() -> false) // Start as false
//                    .persistent(Codec.BOOL)    // Save to NBT as a boolean
//    );

    // 7. INITIALIZATION
    public static void initialize() {
        // Call ModAttachments.initialize() inside your main ModInitializer's onInitialize() method.
        // Accessing the class statics forces the JVM to load the class and fire the registries.
    }
}
