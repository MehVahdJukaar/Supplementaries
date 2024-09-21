package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBuntingBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BuntingItem extends Item {
    public BuntingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(ModRegistry.ROPE.get())) {
            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false);
            //we must place valid state immediately
            BlockState s = RopeBuntingBlock.fromRope(state, hit);
            if (s != null) {
                level.setBlockAndUpdate(pos, s);
                var ret = s.useItemOn(context.getItemInHand(), level, context.getPlayer(), context.getHand(), hit);
                if (!ret.consumesAction()) {
                    level.setBlockAndUpdate(pos, state);
                }
                else return ret.result();
            }
        }
        return super.useOn(context);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        var des = super.getDescriptionId(stack);
        des += "_" + getColor(stack).getName();
        return des;
    }

    public static DyeColor getColor(ItemStack item) {
        return item.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
    }

    public static void setColor(ItemStack item, DyeColor color) {
        item.set(DataComponents.BASE_COLOR, color);
    }

    public static ItemStack getColored(DyeColor color) {
        ItemStack stack = new ItemStack(ModRegistry.BUNTING.get());
        setColor(stack, color);
        return stack;
    }

}
