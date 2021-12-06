package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HangingFlowerPotBlockTile extends SwayingBlockTile implements IBlockHolder, IOwnerProtected {
    private UUID owner = null;

    private BlockState pot = Blocks.FLOWER_POT.defaultBlockState();

    public HangingFlowerPotBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HANGING_FLOWER_POT_TILE.get(), pos, state);
    }

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2f;
        maxPeriod = 35f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return pot;
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        if (state.getBlock() instanceof FlowerPotBlock) {
            this.pot = state;
            this.setChanged();
            //TODO: optimize mark dirty and block update to send only what's needed
            if (this.level != null)
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        //if(pot != Blocks.AIR.getDefaultState())
        tag.put("Pot", NbtUtils.writeBlockState(pot));
        this.saveOwner(tag);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        pot = NbtUtils.readBlockState(compound.getCompound("Pot"));
        this.loadOwner(compound);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition);
    }
}
