package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.BuntingBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
            BlockState s = BuntingBlock.fromRope(state);
            level.setBlockAndUpdate(pos, s);
            BlockState newState = level.getBlockState(pos);
            if (!newState.is(state.getBlock())) {
                var ret = newState.use(level, context.getPlayer(), context.getHand(),
                        new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false));
                if (!ret.consumesAction()) {
                    level.setBlockAndUpdate(pos, state);
                }
                return ret;
            }
        }
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

    public static DyeColor getColor(ItemStack item) {
        if (item.getItem() instanceof BuntingItem) {
            CompoundTag tag = item.getTag();
            if (tag != null) {
                return DyeColor.byName(tag.getString("Color"), DyeColor.WHITE);
            }
        }
        return DyeColor.WHITE;
    }
}
