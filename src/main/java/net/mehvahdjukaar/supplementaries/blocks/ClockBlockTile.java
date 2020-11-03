package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public class ClockBlockTile extends TileEntity implements ITickableTileEntity {
    public float roll = 0;
    public float prevRoll = 0;
    public float targetRoll = 0;

    public ClockBlockTile() {
        super(Registry.CLOCK_BLOCK_TILE.get());
    }

    @Override
    public void read(@Nonnull BlockState state,@Nonnull CompoundNBT compound) {
        super.read(state, compound);
        this.roll = compound.getFloat("roll");
        this.prevRoll = compound.getFloat("prevroll");
        this.targetRoll = compound.getFloat("targetroll");
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        super.write(compound);
        compound.putFloat("roll", this.roll);
        compound.putFloat("prevroll", this.prevRoll);
        compound.putFloat("targetroll", this.targetRoll);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }

    public void setInitialRoll(int hour) {
        this.targetRoll = (30f * hour) % 360;
        this.prevRoll = this.targetRoll;
        this.roll = this.targetRoll;
    }

    public void tick() {
        //TODO:update tile this on placement logic
        if (this.world != null && this.world.getGameTime() % 20L == 0L) {
            BlockState blockstate = this.getBlockState();
            if (!this.world.isRemote) {
                Block block = blockstate.getBlock();
                if (block instanceof ClockBlock) {
                    ClockBlock.updatePower(blockstate, this.world, this.pos);
                }
            }
            this.targetRoll = (30f * ClockBlock.getHour(blockstate)) % 360;
        }
        this.prevRoll = this.roll;
        if (this.roll != this.targetRoll) {
            float r = (this.roll + 8) % 360;
            if (r >= this.targetRoll && r <= this.targetRoll + 8) {
                r = this.targetRoll;
            }
            this.roll = r;
        }
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }
}

