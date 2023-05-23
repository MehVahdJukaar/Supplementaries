package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FeatherBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.IFrameBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class FrameBlockTile extends MimicBlockTile {

    public FrameBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TIMBER_FRAME_TILE.get(), pos, state);
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        this.mimic = state;

        if (this.level instanceof ServerLevel) {
            this.setChanged();
            int newLight = SuppPlatformStuff.getLightEmission(this.getHeldBlock(), level, worldPosition);
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FrameBlock.HAS_BLOCK, true)
                    .setValue(FrameBlock.WATERLOGGED, false)
                    .setValue(FrameBlock.LIGHT_LEVEL, newLight), 3);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    /**
     * returns new modified or contained state, null if failed
     * unchecked. call isValidBlock first
     */
    public BlockState acceptBlock(BlockState state) {
        Block b = state.getBlock();

        if (b == ModRegistry.DAUB.get() && CommonConfigs.Building.REPLACE_DAUB.get()) {
            if (level != null && !this.level.isClientSide && this.getBlockState().getBlock() instanceof IFrameBlock fb) {
                var s = fb.getFilledBlock(state.getBlock());
                if (s != null) {
                    state = s.withPropertiesOf(this.getBlockState());
                    this.level.setBlock(this.worldPosition, state, 3);
                }
            }
        } else {
            this.setHeldBlock(state);
            //called here to update immediately to prevent glitching with different model shaped
            if (level != null && this.level.isClientSide) {
                this.requestModelReload();
            }
        }
        return state;
    }

    public InteractionResult handleInteraction(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace, boolean canStrip) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (Utils.mayBuild(player,pos)) {
            if (item instanceof BlockItem blockItem && this.getHeldBlock().isAir()) {
                BlockState toPlace = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, stack, trace));

                if (isValidBlock(toPlace, pos, level)) {

                    BlockState newState = this.acceptBlock(toPlace);

                    SoundType s = newState.getSoundType();
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    level.playSound(player, pos, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild && !level.isClientSide()) {
                        stack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else if (canStrip && item instanceof AxeItem && !this.getHeldBlock().isAir() && CommonConfigs.Building.AXE_TIMBER_FRAME_STRIP.get()) {
                BlockState held = this.getHeldBlock();
                if (!level.isClientSide) {
                    Block.popResourceFromFace(level, pos, trace.getDirection(), new ItemStack(this.getBlockState().getBlock()));
                }
                level.playSound(player, pos, this.getBlockState().getSoundType().getBreakSound(),
                        SoundSource.BLOCKS, 1, 1);
                stack.hurtAndBreak(1, player, (l) -> l.broadcastBreakEvent(hand));
                level.setBlockAndUpdate(pos, held);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        //don't try filling with other hand
        return InteractionResult.FAIL;
    }

    public static boolean isValidBlock(@Nullable BlockState state, BlockPos pos, Level world) {
        if (state == null) return false;
        Block b = state.getBlock();

        if (b.builtInRegistryHolder().is(ModTags.FRAME_BLOCK_BLACKLIST) || b instanceof EntityBlock) {
            return false;
        }
        if (b instanceof FeatherBlock || b instanceof SoulSandBlock) return true;
        return state.isSolidRender(world, pos) && Block.isShapeFullBlock(state.getCollisionShape(world, pos));
    }
}

