package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.common.util.Constants;

import java.util.function.Supplier;

public class FrameBlockTile extends MimicBlockTile {

    public FrameBlockTile() {
        super(Registry.TIMBER_FRAME_TILE.get());
        //data = new ModelDataMap.Builder().withInitial(MIMIC, held).build();
    }

    @Override
    public boolean setHeldBlock(BlockState state) {
        //int oldLight = this.getLightValue();
        this.mimic = state;

        if(!this.level.isClientSide) {
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        //TODO: REMOVE
        if(compound.contains("Held"))
            this.mimic = NBTUtil.readBlockState(compound.getCompound("Held"));
    }

    public int getLightValue(){
        return this.getHeldBlock().getLightEmission();
    }

    public ActionResultType handleInteraction(PlayerEntity player, Hand hand, BlockRayTraceResult trace, Supplier<Block> daub) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if(item instanceof BlockItem && this.getHeldBlock().isAir()) {
            boolean success = false;
            Block b = ((BlockItem) item).getBlock();
            BlockState state = b.defaultBlockState();
            //TODO: add blacklist here
            if (b == Registry.DAUB_FRAME.get() || b == Registry.DAUB_BRACE.get() || b == Registry.DAUB_CROSS_BRACE.get()){
                return ActionResultType.PASS;
            }
            else if (b == Registry.DAUB.get() && ServerConfigs.cached.REPLACE_DAUB) {
                state = daub.get().defaultBlockState();
                if (this.getBlockState().hasProperty(BlockProperties.FLIPPED)) {
                    state = state.setValue(BlockProperties.FLIPPED, this.getBlockState().getValue(BlockProperties.FLIPPED));
                }
                this.level.setBlock(this.worldPosition, state, 3);
                success = true;
            }
            else if (isValidBlock(state)) {
                state = b.getStateForPlacement(new BlockItemUseContext(player, hand, stack, trace));
                this.setHeldBlock(state);
                if (level.isClientSide()) {
                    ModelDataManager.requestModelDataRefresh(this);
                }
                success = true;
            }
            if(success){
                SoundType s = state.getSoundType(level, worldPosition, player);
                this.level.playSound(player, worldPosition, s.getPlaceSound(), SoundCategory.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                if (!player.isCreative() && !level.isClientSide()) {
                    stack.shrink(1);
                }
                return ActionResultType.sidedSuccess(this.level.isClientSide);
            }
        }
        //don't try filling with other hand
        //TODO: use correctly FAIL and PASS
        return ActionResultType.FAIL;
    }

    private boolean isValidBlock(BlockState state) {
        Block block = state.getBlock();
        //if (BLOCK_BLACKLIST.contains(block)) { return false; }
        if (block.hasTileEntity(state)) { return false; }
        return state.isSolidRender(this.level, this.worldPosition);
    }

}

