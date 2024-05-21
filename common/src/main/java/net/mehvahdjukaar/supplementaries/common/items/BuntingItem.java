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
            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false);
            //we must place valid state immediately
            BlockState s = BuntingBlock.fromRope(state, hit);
            if (s != null) {
                level.setBlockAndUpdate(pos, s);
                var ret = s.use(level, context.getPlayer(), context.getHand(), hit);
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

    public static ItemStack getColored(DyeColor color) {
        ItemStack stack = new ItemStack(ModRegistry.BUNTING.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Color", color.getName());
        return stack;
    }
}
