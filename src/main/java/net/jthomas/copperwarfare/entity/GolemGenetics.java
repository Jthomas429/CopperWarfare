package net.jthomas.copperwarfare.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class GolemGenetics {

    public static GolemTraits forgeOffspringTraits(GolemTraits parentA, GolemTraits parentB, RandomSource random) {

        // --- Mutation magnitudes ---
        float PRIMARY_MUT  = 0.40f; // Strength: wide variance, the stat to chase
        float STYLE_MUT    = 0.20f; // How much the combat archetype can shift per generation
        float BUDGET_MUT   = 0.15f; // How much total combat power can drift per generation
        float TERTIARY_MUT = 0.15f; // Independent wobble for speed and size

        // -----------------------------------------------------------------------
        // STRENGTH — fully independent, the one pure upside stat
        // -----------------------------------------------------------------------
        float avgStrength   = (parentA.strength() + parentB.strength()) / 2.0f;
        float strengthMut   = (random.nextFloat() - 0.5f) * 2 * PRIMARY_MUT;
        float finalStrength = avgStrength * (1.0f + strengthMut);

        // -----------------------------------------------------------------------
        // COMBAT STYLE — inherited from each parent's actual atkSpeed/hp ratio.
        // log(atkSpd / hp) is 0 when balanced, positive for glass cannon, negative for tank.
        // Averaging two log ratios gives a sensible genetic midpoint.
        // -----------------------------------------------------------------------
        float styleA = (float) Math.log(parentA.atkSpeed() / parentA.hp());
        float styleB = (float) Math.log(parentB.atkSpeed() / parentB.hp());
        float inheritedStyle = (styleA + styleB) / 2.0f;
        float styleMut   = (random.nextFloat() - 0.5f) * 2 * STYLE_MUT;
        float finalStyle = inheritedStyle + styleMut;

        // -----------------------------------------------------------------------
        // COMBAT BUDGET — the total "combat power" (geometric mean of atkSpd * hp).
        // Preserved across style changes so rebalancing isn't a free lunch.
        // A tank and a glass cannon with the same budget are equally dangerous overall.
        // -----------------------------------------------------------------------
        float budgetA   = (float) Math.sqrt(parentA.atkSpeed() * parentA.hp());
        float budgetB   = (float) Math.sqrt(parentB.atkSpeed() * parentB.hp());
        float avgBudget = (budgetA + budgetB) / 2.0f;
        float budgetMut  = (random.nextFloat() - 0.5f) * 2 * BUDGET_MUT;
        float finalBudget = avgBudget * (1.0f + budgetMut);

        // Reconstruct atkSpd and hp from the inherited style and budget.
        // exp(+style/2) and exp(-style/2) are perfect inverses, so their product
        // always equals finalBudget² — the tradeoff is mathematically exact.
        float finalAtkSpd = finalBudget * (float) Math.exp( finalStyle / 2.0f);
        float finalHp     = finalBudget * (float) Math.exp(-finalStyle / 2.0f);

        // -----------------------------------------------------------------------
        // TERTIARY: Speed and size are inherited from parents directly,
        // then gently pulled by the combat style axis with independent wobble.
        // Glass cannons trend fast and small; tanks trend slow and large.
        // -----------------------------------------------------------------------
        float avgSpeed = (parentA.speed() + parentB.speed()) / 2.0f;
        float avgSize  = (parentA.size()  + parentB.size())  / 2.0f;

        float speedWobble = (random.nextFloat() - 0.5f) * 2 * TERTIARY_MUT;
        float sizeWobble  = (random.nextFloat() - 0.5f) * 2 * TERTIARY_MUT;

        // finalStyle is already in log-space so we use it as a gentle additive nudge.
        // 0.3f coupling means a strongly glass cannon style (~0.6 log units) shifts
        // speed/size by roughly 18% — noticeable but not deterministic.
        float finalSpeed = avgSpeed * (1.0f + finalStyle * 0.3f + speedWobble);
        float finalSize  = avgSize  * (1.0f - finalStyle * 0.3f + sizeWobble);

        // --- Clamp and return ---
        return new GolemTraits(
                Mth.clamp(finalSpeed,    0.5f, 5.0f),
                Mth.clamp(finalAtkSpd,   0.2f, 5.0f),
                Mth.clamp(finalStrength, 0.1f, 10.0f),
                Mth.clamp(finalHp,       0.2f, 5.0f),
                Mth.clamp(finalSize,     0.1f, 10.0f)
        );
    }
}
