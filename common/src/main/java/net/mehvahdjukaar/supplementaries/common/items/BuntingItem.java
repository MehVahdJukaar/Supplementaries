package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBuntingBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class BuntingItem extends StandingAndWallBlockItem implements IColored {

    private final DyeColor dyeColor;

    public BuntingItem(DyeColor color, Block block, Block wallBlock, Properties properties, Direction attachmentDirection) {
        super(block, wallBlock, properties, attachmentDirection);
        this.dyeColor = color;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!MiscUtils.showsHints(tooltipFlag)) return;
        tooltipComponents.add((Component.translatable("message.supplementaries.bunting")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
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
    public DyeColor getColor() {
        return dyeColor;
    }
}
