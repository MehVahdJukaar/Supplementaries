package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.LaserBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.Random;


public class LaserBlockTile extends TileEntity implements ITickableTileEntity {
    public BlockPos endpos = null; // block that the laser is touching
    public int lenght = 0;
    public float offset = -1;
    public float prevWidth = 0;
    public float width = 0;
    public static final int MAXLENGHT = 15;
    public LaserBlockTile() {
        super(ModRegistry.LASER_BLOCK_TILE.get());
    }

    // TODO:cache the blockposition on a list for faster accsssing
    // this is already server only
    public void updateBeam() {
        if (this.canEmit()) {
            BlockPos p = this.worldPosition;
            Direction dir = this.getDirection();
            int i = 0;
            boolean noblockfound = false;
            for (i = 0; i <= MAXLENGHT; i++) {
                p = this.worldPosition.relative(dir, i + 1);
                BlockState state = this.level.getBlockState(p);
                if (state.getLightBlock(this.level, p) < 15)
                    continue;
                if (state.isFaceSturdy(level, p, dir.getOpposite())) {
                    noblockfound = false;
                    break;
                }
            }
            if (this.lenght != i) {
                this.lenght = i;
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
            }
            if (noblockfound) {
                this.endpos = null;
                i = MAXLENGHT + 1;
            } else {
                this.endpos = p;
                this.updateReceivingLaser();
            }
        }
    }

    public void updateReceivingLaser() {
        if (endpos != null) {
            BlockState state = this.level.getBlockState(this.endpos);
            if (state.getBlock() instanceof LaserBlock
                    && state.getValue(LaserBlock.RECEIVING) != MathHelper.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)
                    && state.getValue(LaserBlock.FACING) == this.getBlockState().getValue(LaserBlock.FACING).getOpposite()) {
                this.level.setBlock(this.endpos, state.setValue(LaserBlock.RECEIVING, MathHelper.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)), 3);
            }
        }
    }// TODO:o check if null

    public void turnOffReceivingLaser() {
        if (endpos != null) {
            BlockState state = this.level.getBlockState(this.endpos);
            if (state.getBlock() instanceof LaserBlock && state.getValue(LaserBlock.RECEIVING) != 0
                    && state.getValue(LaserBlock.FACING) == this.getBlockState().getValue(LaserBlock.FACING).getOpposite()) {
                this.level.setBlock(this.endpos, state.setValue(LaserBlock.RECEIVING, 0), 3);
            }
        }
    }

    public boolean canEmit() {
        return this.isPowered() && !this.isReceiving();
    }

    public boolean isReceiving() {
        return this.getBlockState().getValue(LaserBlock.RECEIVING) > 0;
    }

    public boolean isPowered() {
        return this.getBlockState().getValue(LaserBlock.POWERED);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
        //return new AxisAlignedBB(this.pos.add(-20,-20,-20), this.pos.add(20,20,20));
        //return CommonUtil.getDirectionBB(this.pos, this.getDirection(), this.lenght);
    }

    @Override
    public double getViewDistance() {
        return 128;
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            if (this.offset == -1)
                this.offset = (new Random(this.getBlockPos().asLong())).nextFloat() * (float) Math.PI * 2f;
            this.prevWidth = this.width;
            float angle = this.offset + (this.getLevel().getGameTime()%24000) / 50f;
            this.width = MathHelper.sin(angle % (float) Math.PI * 2f);
        } else if (this.level != null && this.level.getGameTime() % 20L == 0L) {
            this.updateBeam();
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.lenght = compound.getInt("Length");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("Length", this.lenght);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
        this.updateBeam();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(LaserBlock.FACING);
    }
}