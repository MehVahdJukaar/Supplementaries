package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

@Deprecated(forRemoval = true)
public class BuntingItemOld extends Item {
    public BuntingItemOld(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!MiscUtils.showsHints(tooltipFlag)) return;
        tooltipComponents.add((Component.translatable("message.supplementaries.bunting")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        var newStack = stack.transmuteCopy(ModRegistry.BUNTING_BLOCKS.get(getColor(stack)).get().asItem());
        newStack.remove(DataComponents.BASE_COLOR); //remove color component to avoid duplication
        entity.getSlot(slotId).set(newStack);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "block.supplementaries_bunting_" + getColor(stack).getName();
    }

    public static DyeColor getColor(ItemStack item) {
        return item.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
    }

    public static void setColor(ItemStack item, DyeColor color) {
        item.set(DataComponents.BASE_COLOR, color);
    }

    public static ItemStack getColored(DyeColor color) {
        ItemStack stack = new ItemStack(ModRegistry.BUNTING_OLD.get());
        setColor(stack, color);
        return stack;
    }

}
