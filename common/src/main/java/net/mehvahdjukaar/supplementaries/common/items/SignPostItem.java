package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SignPostWallBlock;
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

public class SignPostItem extends WoodBasedBlockItem {

    public SignPostItem(Block block, Properties properties, WoodType wood) {
        super(block, properties, wood, 100);
    }

    private AttachType getAttachType(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof SignPostWallBlock) return AttachType.WAY_SIGN_WALL;
        if (b instanceof SignPostBlock) return AttachType.WAY_SIGN_POST;
        else if ((b instanceof StickBlock && !state.getValue(StickBlock.AXIS_X) && !state.getValue(StickBlock.AXIS_Z))
                || (state.getBlock() instanceof EndRodBlock && state.getValue(EndRodBlock.FACING).getAxis() == Direction.Axis.Y)) {
            return AttachType.STICK;
        }
        ResourceLocation res = Utils.getID(b);
        //hardcoding this one
        if (state.is(ModTags.POSTS) && !res.getNamespace().equals("blockcarpentry")) return AttachType.FENCE;
        return AttachType.WALL;
    }

    private enum AttachType {
        FENCE, STICK, WALL, WAY_SIGN_POST, WAY_SIGN_WALL;

        int getRot(UseOnContext context) {
            if (!this.needsConversion()) {
                return 0;
            } else {
                return Mth.floor(((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15;
            }
        }

        public boolean needsConversion() {
            return this == FENCE || this == STICK;
        }

        public boolean isSign() {
            return (this == WAY_SIGN_POST || this == WAY_SIGN_WALL);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockState state = level.getBlockState(blockpos);
        Block targetBlock = state.getBlock();

        boolean framed = false;
        var attachType = getAttachType(state);
        int rotation = attachType.getRot(context);

        if (attachType.needsConversion()) {
            if (CompatHandler.FRAMEDBLOCKS) {
                Block f = FramedBlocksCompat.tryGettingFramedBlock(targetBlock, level, blockpos);
                if (f != null) {
                    framed = true;
                    if (f != Blocks.AIR) targetBlock = f;
                }
            }
            boolean waterlogged = level.getFluidState(blockpos).getType() == Fluids.WATER;
            level.setBlock(blockpos, ModRegistry.SIGN_POST.get()
                    .getStateForPlacement(new BlockPlaceContext(context)).setValue(SignPostBlock.WATERLOGGED, waterlogged), 3);
        }
        //normal wall placement
        else if (attachType == AttachType.WALL) {
            if (super.useOn(context).consumesAction()) {
                context = this.updatePlacementContext(new BlockPlaceContext(context));
                blockpos = context.getClickedPos();
            } else return InteractionResult.PASS;
        }


        if (level.getBlockEntity(blockpos) instanceof SignPostBlockTile tile) {

            double y = context.getClickLocation().y - blockpos.getY();
            boolean up = y > 0.5d;

            boolean addedSign = tile.trySetSign(this.getBlockType(), rotation, up, framed);

            if (addedSign) {
                if (attachType != AttachType.WALL) {
                    if (attachType.needsConversion()) {
                        tile.setHeldBlock(targetBlock.defaultBlockState());
                        BlockUtil.addOptionalOwnership(player, tile);
                        tile.setChanged();
                    }

                    level.sendBlockUpdated(blockpos, state, state, 3);
                    SoundType soundtype = this.getBlockType().getSound();
                    level.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                    if (!context.getPlayer().isCreative()) itemstack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }


}