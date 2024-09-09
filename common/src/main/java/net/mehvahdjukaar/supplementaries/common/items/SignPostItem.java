package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.WoodBasedItem;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

public class SignPostItem extends WoodBasedItem {

    public SignPostItem(Properties properties, WoodType wood) {
        super(properties, wood, 100);
    }

    private AttachType getAttachType(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof SignPostBlock) return AttachType.SIGN_POST;
        else if ((b instanceof StickBlock && !state.getValue(StickBlock.AXIS_X) && !state.getValue(StickBlock.AXIS_Z))
                || (state.getBlock() instanceof EndRodBlock && state.getValue(EndRodBlock.FACING).getAxis() == Direction.Axis.Y)) {
            return AttachType.STICK;
        }
        ResourceLocation res = Utils.getID(b);
        //hardcoding this one
        if (state.is(ModTags.POSTS) && !res.getNamespace().equals("blockcarpentry")) return AttachType.FENCE;
        return AttachType.NONE;
    }

    private enum AttachType {
        FENCE, SIGN_POST, STICK, NONE
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();

        BlockState state = world.getBlockState(blockpos);
        Block targetBlock = state.getBlock();

        boolean framed = false;

        var attachType = getAttachType(state);
        if (attachType != AttachType.NONE) {

            if (CompatHandler.FRAMEDBLOCKS) {
                Block f = FramedBlocksCompat.tryGettingFramedBlock(targetBlock, world, blockpos);
                if (f != null) {
                    framed = true;
                    if (f != Blocks.AIR) targetBlock = f;
                }
            }

            boolean waterlogged = world.getFluidState(blockpos).getType() == Fluids.WATER;
            if (attachType != AttachType.SIGN_POST) {
                world.setBlock(blockpos, ModRegistry.SIGN_POST.get()
                        .getStateForPlacement(new BlockPlaceContext(context)).setValue(SignPostBlock.WATERLOGGED, waterlogged), 3);
            }
            boolean flag = false;

            if (world.getBlockEntity(blockpos) instanceof SignPostBlockTile tile) {


                int r = Mth.floor(((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15;

                double y = context.getClickLocation().y - blockpos.getY();

                boolean up = y > 0.5d;


                flag = tile.initializeSignAfterConversion(this.getBlockType(), r, up,
                        attachType == AttachType.STICK, framed);

                if(flag) {
                    if (attachType != SignPostItem.AttachType.SIGN_POST) {
                        tile.setHeldBlock(targetBlock.defaultBlockState());
                        tile.setChanged();
                    } else {
                        BlockUtil.addOptionalOwnership(player, tile);
                    }
                }

            }
            if (flag) {

                world.sendBlockUpdated(blockpos, state, state, 3);

                SoundType soundtype = this.getBlockType().getSound();
                world.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                world.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                if (!context.getPlayer().isCreative()) itemstack.shrink(1);
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }


}