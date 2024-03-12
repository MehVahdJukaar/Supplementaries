package net.mehvahdjukaar.supplementaries.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

public class BuntingItem extends Item {
    public BuntingItem(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        var des = super.getDescriptionId(stack);
        var t = stack.getTag();
        if (t != null) {
            des += "_" + t.getString("Color");
        }
        return des;
    }

    @Nullable
    public static DyeColor getColor(ItemStack item) {
        if (item.getItem() instanceof BuntingItem) {
            CompoundTag tag = item.getTag();
            if (tag == null) return DyeColor.WHITE;
            return DyeColor.valueOf(tag.getString("Color"));
        }
        return null;
    }
}
