package net.jthomas.copperwarfare.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class GolemGenetics {

    public static GolemTraits forgeOffspringTraits(GolemTraits parentA, GolemTraits parentB, RandomSource random) {

        // 1a. Calculate the genetic averages
        float avgSize = (parentA.size() + parentB.size()) / 2.0f;
        float avgStrength = (parentA.strength() + parentB.strength()) / 2.0f;
        float avgSpeed = (parentA.speed() + parentB.speed()) / 2.0f;
        float avgHp = (parentA.hp() + parentB.hp()) / 2.0f;
        float avgAtkSpd = (parentA.atkSpeed() + parentB.atkSpeed()) / 2.0f;

        // 1b. Mutation Size
        float mutationLarge = 0.50f;
        float mutationMedium = 0.30f;
        float mutationSmall = 0.15f;

        // 2a. Apply primary mutations (Random float between +/- mutation size (-0.50 and +0.50 for large)
        float sizeMutation = (random.nextFloat() - 0.50f) * (mutationLarge * 2);
        float strengthMutation = (random.nextFloat() - 0.50f) * (mutationLarge * 2); //was medium
        float speedMutation = (random.nextFloat() - 0.50f) * (mutationMedium * 2);
        float hpMutation = (random.nextFloat() - 0.50f) * (mutationMedium * 2);
        float atkSpdMutation = (random.nextFloat() - 0.50f) * (mutationMedium * 2);

        // 2b. Calculate the trade-off ratio based on how the size mutated
        // If the golem grew to 1.1, sizeModifier is 1.05
        float finalStrength = avgStrength * (1.0f + strengthMutation);
        float finalSize = avgSize * (1.0f + sizeMutation);
        float sizeModifier = (finalSize + 1) / 2;

        // 2c. Apply secondary mutations with physical trade-offs
        float finalSpeed = (avgSpeed * (1.0f + speedMutation)) / sizeModifier;
        float finalAtkSpd = (avgAtkSpd * (1.0f + atkSpdMutation)) / sizeModifier;
        float finalHp = (avgHp * (1.0f + hpMutation)) * sizeModifier;

        // 5. Clamp the values so the game engine doesn't break
        // (e.g., Mth.clamp prevents negative speed or hitboxes larger than a house)
        return new GolemTraits(
//                Mth.clamp(finalSpeed, 0.4f, 2.5f),
//                Mth.clamp(finalStrength, 0.5f, 4.0f),
//                Mth.clamp(finalHp, 0.5f, 5.0f),
//                Mth.clamp(mutatedSize, 0.5f, 3.0f) // 3.0f means 3x as big as vanilla!
                Mth.clamp(finalSpeed, 0.5f, 5.0f),
                Mth.clamp(finalAtkSpd, 0.2f, 5.0f),
                Mth.clamp(finalStrength, 0.1f, 10.0f),
                Mth.clamp(finalHp, 0.1f, 10.0f),
                Mth.clamp(finalSize, 0.1f, 10.0f) // extreme mode
        );
    }
}
