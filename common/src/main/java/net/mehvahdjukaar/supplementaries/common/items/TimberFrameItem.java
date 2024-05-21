package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.FuelBlockItem;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TimberFrameItem extends FuelBlockItem {

    public TimberFrameItem(Block block, Properties properties, int burnTicks) {
        super(block, properties, () -> burnTicks);
    }

    public TimberFrameItem(Block block, Properties properties) {
        this(block, properties, 0);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (CommonConfigs.Building.SWAP_TIMBER_FRAME.get() && player != null && player.isShiftKeyDown() &&
                Utils.mayPerformBlockAction(player, context.getClickedPos(), context.getItemInHand())) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState clicked = world.getBlockState(pos);
            if (FrameBlockTile.isValidBlock(clicked, pos, world)) {
                BlockState frame = this.getBlock().getStateForPlacement(new BlockPlaceContext(context));
                if (frame != null) {
                    world.setBlockAndUpdate(pos, frame);
                    if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
                        SoundType s = frame.getSoundType();
                        tile.acceptBlock(clicked);
                        world.playSound(player, pos, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                        if (!player.isCreative() && !world.isClientSide()) {
                            context.getItemInHand().shrink(1);
                        }
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                    return InteractionResult.FAIL;
                }
            }

        }
        return super.useOn(context);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!MiscUtils.showsHints(worldIn, flagIn)) return;
        tooltip.add((Component.translatable("message.supplementaries.timber_frame")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
