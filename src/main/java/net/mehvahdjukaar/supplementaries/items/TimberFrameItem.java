package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class TimberFrameItem extends BlockItem {


    public TimberFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (ServerConfigs.cached.SWAP_TIMBER_FRAME && player.isShiftKeyDown() && player.abilities.mayBuild) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState clicked = world.getBlockState(pos);
            if (FrameBlockTile.isValidBlock(clicked, pos, world)) {
                BlockState frame = this.getBlock().getStateForPlacement(new BlockPlaceContext(context));
                world.setBlockAndUpdate(pos, frame);
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof FrameBlockTile) {
                    SoundType s = frame.getSoundType(world, pos, player);
                    ((FrameBlockTile) tile).acceptBlock(clicked);
                    world.playSound(player, pos, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                    if (!player.isCreative() && !world.isClientSide()) {
                        context.getItemInHand().shrink(1);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
                return InteractionResult.FAIL;
            }

        }
        return super.useOn(context);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 200;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips) return;
        tooltip.add((new TranslatableComponent("message.supplementaries.timber_frame")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
