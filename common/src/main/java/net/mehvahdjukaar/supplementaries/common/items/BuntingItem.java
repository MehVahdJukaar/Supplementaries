package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBuntingBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BuntingItem extends Item {
    public BuntingItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return getColored(DyeColor.WHITE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!MiscUtils.showsHints(worldIn, flagIn)) return;
        tooltip.add((Component.translatable("message.supplementaries.bunting")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
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

    public static void setColor(ItemStack item, DyeColor color) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putString("Color", color.getName());
    }

    public static ItemStack getColored(DyeColor color) {
        ItemStack stack = new ItemStack(ModRegistry.BUNTING.get());
        setColor(stack, color);
        return stack;
    }
}
