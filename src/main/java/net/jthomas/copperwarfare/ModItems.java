package net.jthomas.copperwarfare;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.jthomas.copperwarfare.entity.OverhauledCopperGolem;
import net.jthomas.copperwarfare.item.TuningRodItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.jthomas.copperwarfare.Copperwarfare.MOD_ID;

public class ModItems {

    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Item TUNING_ROD = registerItem("tuning_rod",
            (props) -> new TuningRodItem(props
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.literal("Rod of Tuning").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                            Component.literal("Warning - Angers all nearby Cooper Golems").withStyle(ChatFormatting.GOLD)
                    )))
            ));

    public static Item registerItem(String name, Function<Item.Properties, Item> function) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = function.apply(new Item.Properties().setId(key));

        ITEMS.add(item);
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    public static void registerModItems() {
        PolymerItemGroupUtils.registerPolymerItemGroup(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"),
                CreativeModeTab.builder(null, -1)
                        .title(Component.literal("Jordan's Modded Items"))
                        .icon(() -> new ItemStack(ModItems.TUNING_ROD))
                        .displayItems((params, output) -> {
                            for (Item item : ITEMS) {
                                if (!(item instanceof SpawnEggItem)) {
                                    output.accept(item);
                                }
                            }
                            for (Item item : ITEMS) {
                                if (item instanceof SpawnEggItem) {
                                    output.accept(item);
                                }
                            }
                        })
                        .build()
        );
    }
}
