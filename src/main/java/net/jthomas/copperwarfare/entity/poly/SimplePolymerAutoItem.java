package net.jthomas.copperwarfare.entity.poly;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.packettweaker.PacketContext;

public class SimplePolymerAutoItem extends Item implements PolymerAutoItem {
    private final Item polymerItem;

    public SimplePolymerAutoItem(Properties properties, Item polymerItem) {
        super(properties);
        this.polymerItem = polymerItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerItem;
    }
}
