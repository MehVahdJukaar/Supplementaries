package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FeatherBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FrameBlockTile extends MimicBlockTile {

    public final Lazy<BlockState> WATTLE_AND_DAUB = Lazy.of(() -> ((FrameBlock) this.getBlockState().getBlock()).daub.get().defaultBlockState());

    public FrameBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, () -> null);
    }

    public FrameBlockTile(BlockPos pos, BlockState state, Supplier<Block> wattle_and_daub) {
        super(ModRegistry.TIMBER_FRAME_TILE.get(), pos, state);
        //data = new ModelDataMap.Builder().withInitial(MIMIC, held).build();
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        //int oldLight = this.getLightValue();
        this.mimic = state;

        if (this.level instanceof ServerLevel) {
            this.setChanged();
            int newLight = this.getLightValue();
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FrameBlock.HAS_BLOCK, true)
                    .setValue(FrameBlock.LIGHT_LEVEL, newLight), 3);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
        //if (this.getLightValue() != oldLight) this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
        return true;
    }

    public int getLightValue() {
        return this.getHeldBlock().getLightEmission();
    }

    /**
     * returns new modified or contained state, null if failed
     * unchecked. call isValidBlock first
     */
    public BlockState acceptBlock(BlockState state) {
        Block b = state.getBlock();

        if (b == ModRegistry.DAUB.get() && ServerConfigs.cached.REPLACE_DAUB) {
            if (level != null && !this.level.isClientSide) {
                state = WATTLE_AND_DAUB.get();
                if (this.getBlockState().hasProperty(BlockProperties.FLIPPED)) {
                    state = state.setValue(BlockProperties.FLIPPED, this.getBlockState().getValue(BlockProperties.FLIPPED));
                }
                this.level.setBlock(this.worldPosition, state, 3);
            }
        } else {
            this.setHeldBlock(state);
            if (level != null && level.isClientSide()) {
                ModelDataManager.requestModelDataRefresh(this);
            }
        }
        return state;
    }

    public InteractionResult handleInteraction(Player player, InteractionHand hand, BlockHitResult trace) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (player.getAbilities().mayBuild && item instanceof BlockItem blockItem && this.getHeldBlock().isAir()) {

            BlockState toPlace = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, stack, trace));

            if (isValidBlock(toPlace, this.worldPosition, this.level)) {

                BlockState newState = this.acceptBlock(toPlace);

                SoundType s = newState.getSoundType(level, worldPosition, player);
                this.level.gameEvent(player, GameEvent.BLOCK_CHANGE, this.worldPosition);
                this.level.playSound(player, this.worldPosition, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                if (!player.isCreative() && !level.isClientSide()) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level.isClientSide);

            }
        }
        //don't try filling with other hand
        return InteractionResult.FAIL;
    }

    public static boolean isValidBlock(@Nullable BlockState state, BlockPos pos, Level world) {
        if (state == null) return false;
        Block b = state.getBlock();
        if (b == Blocks.BEDROCK) return false;
        if (b == ModRegistry.DAUB_FRAME.get() || b == ModRegistry.DAUB_BRACE.get() || b == ModRegistry.DAUB_CROSS_BRACE.get())
            return false;
        //if (BLOCK_BLACKLIST.contains(block)) { return false; }
        if (b instanceof EntityBlock) {
            return false;
        }
        if (b instanceof FeatherBlock) return true;
        return state.isSolidRender(world, pos) && Block.isShapeFullBlock(state.getCollisionShape(world, pos));
    }

}

