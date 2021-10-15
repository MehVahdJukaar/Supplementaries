package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Lazy;

import java.util.function.Supplier;

public class FrameBlockTile extends MimicBlockTile {

    public final Lazy<BlockState> WATTLE_AND_DAUB = Lazy.of(() -> ((FrameBlock) this.getBlockState().getBlock()).daub.get().defaultBlockState());

    public FrameBlockTile() {
        this(() -> null);
    }

    public FrameBlockTile(Supplier<Block> wattle_and_daub) {
        super(ModRegistry.TIMBER_FRAME_TILE.get());

        //data = new ModelDataMap.Builder().withInitial(MIMIC, held).build();
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        //int oldLight = this.getLightValue();
        this.mimic = state;

        if (!this.level.isClientSide) {
            this.setChanged();
            int newLight = this.getLightValue();
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FrameBlock.HAS_BLOCK, true)
                    .setValue(FrameBlock.LIGHT_LEVEL, newLight), 3);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
        //if (this.getLightValue() != oldLight) this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
        return true;
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        //TODO: REMOVE
        if (compound.contains("Held"))
            this.mimic = NbtUtils.readBlockState(compound.getCompound("Held"));
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
            if (!this.level.isClientSide) {
                state = WATTLE_AND_DAUB.get();
                if (this.getBlockState().hasProperty(BlockProperties.FLIPPED)) {
                    state = state.setValue(BlockProperties.FLIPPED, this.getBlockState().getValue(BlockProperties.FLIPPED));
                }
                this.level.setBlock(this.worldPosition, state, 3);
            }
        } else {
            this.setHeldBlock(state);
            if (level.isClientSide()) {
                ModelDataManager.requestModelDataRefresh(this);
            }
        }
        return state;
    }

    public InteractionResult handleInteraction(Player player, InteractionHand hand, BlockHitResult trace) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (player.abilities.mayBuild && item instanceof BlockItem && this.getHeldBlock().isAir()) {

            BlockState toPlace = ((BlockItem) item).getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, stack, trace));

            if (isValidBlock(toPlace, this.worldPosition, this.level)) {

                BlockState newState = this.acceptBlock(toPlace);

                SoundType s = newState.getSoundType(level, worldPosition, player);
                this.level.playSound(player, worldPosition, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                if (!player.isCreative() && !level.isClientSide()) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level.isClientSide);

            }
        }
        //don't try filling with other hand
        return InteractionResult.FAIL;
    }

    public static boolean isValidBlock(BlockState state, BlockPos pos, Level world) {
        Block b = state.getBlock();
        if (b == Blocks.BEDROCK) return false;
        if (b == ModRegistry.DAUB_FRAME.get() || b == ModRegistry.DAUB_BRACE.get() || b == ModRegistry.DAUB_CROSS_BRACE.get())
            return false;
        //if (BLOCK_BLACKLIST.contains(block)) { return false; }
        if (b.hasTileEntity(state)) {
            return false;
        }
        return state.isSolidRender(world, pos);
    }

}

