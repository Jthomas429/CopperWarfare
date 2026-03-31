package net.jthomas.copperwarfare.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// A record automatically creates getters like speed() and size() for us
public record GolemTraits(float speed, float atkSpeed, float strength, float hp, float size) {

    // The Codec is the "translator" that saves your stats to the world file
    public static final Codec<GolemTraits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(GolemTraits::speed),
            Codec.FLOAT.fieldOf("atkSpeed").forGetter(GolemTraits::atkSpeed),
            Codec.FLOAT.fieldOf("strength").forGetter(GolemTraits::strength),
            Codec.FLOAT.fieldOf("hp").forGetter(GolemTraits::hp),
            Codec.FLOAT.fieldOf("size").forGetter(GolemTraits::size)
    ).apply(instance, GolemTraits::new));
}