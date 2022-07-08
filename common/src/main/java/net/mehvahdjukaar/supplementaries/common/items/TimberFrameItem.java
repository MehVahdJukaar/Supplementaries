package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class TimberFrameItem extends BlockItem {


    public TimberFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (ServerConfigs.Blocks.SWAP_TIMBER_FRAME.get() && player != null && player.isShiftKeyDown() && player.getAbilities().mayBuild) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState clicked = world.getBlockState(pos);
            if (FrameBlockTile.isValidBlock(clicked, pos, world)) {
                BlockState frame = this.getBlock().getStateForPlacement(new BlockPlaceContext(context));
                if(frame != null) {
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

    @PlatformOnly(PlatformOnly.FORGE)
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 200;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.General.TOOLTIP_HINTS.get() || !flagIn.isAdvanced()) return;
        tooltip.add((Component.translatable("message.supplementaries.timber_frame")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
