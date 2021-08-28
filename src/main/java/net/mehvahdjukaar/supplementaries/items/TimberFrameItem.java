package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TimberFrameItem extends BlockItem {


    public TimberFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if(ServerConfigs.cached.SWAP_TIMBER_FRAME && player.isShiftKeyDown() && player.abilities.mayBuild){
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState clicked = world.getBlockState(pos);
            if(FrameBlockTile.isValidBlock(clicked, pos, world)){
                BlockState frame = this.getBlock().getStateForPlacement(new BlockItemUseContext(context));
                world.setBlockAndUpdate(pos, frame);
                TileEntity tile = world.getBlockEntity(pos);
                if(tile instanceof FrameBlockTile){
                    ((FrameBlockTile) tile).acceptBlock(clicked);
                    SoundType s = frame.getSoundType(world, pos, player);
                    world.playSound(player, pos, s.getPlaceSound(), SoundCategory.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                    if (!player.isCreative() && !world.isClientSide()) {
                        context.getItemInHand().shrink(1);
                    }
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
                return ActionResultType.FAIL;
            }

        }
        return super.useOn(context);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 200;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
        tooltip.add((new TranslationTextComponent(  "message.supplementaries.timber_frame")).withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
    }
}
