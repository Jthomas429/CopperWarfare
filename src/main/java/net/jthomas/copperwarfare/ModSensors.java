package net.jthomas.copperwarfare;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.entity.ai.TuningRodSensor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.sensing.SensorType;

import static net.jthomas.copperwarfare.Copperwarfare.MOD_ID;


public class ModSensors {

    public static final ResourceKey<SensorType<?>> TUNING_ROD_SENSOR_KEY =
            ResourceKey.create(Registries.SENSOR_TYPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, "tuning_rod_sensor"));

    public static final SensorType<TuningRodSensor> TUNING_ROD_SENSOR =
            Registry.register(BuiltInRegistries.SENSOR_TYPE,
                    TUNING_ROD_SENSOR_KEY,
                    new SensorType<>(TuningRodSensor::new));


//    public static final SensorType<TuningRodSensor> TUNING_ROD_SENSOR =
//            Registry.register(
//                    BuiltInRegistries.SENSOR_TYPE,
//                    Identifier.fromNamespaceAndPath(MOD_ID, "tuning_rod_sensor"),
//                    new SensorType<>(TuningRodSensor::new)
//            );

    public static void register() {}
}
