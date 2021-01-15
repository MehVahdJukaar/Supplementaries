package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.LaserBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
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
        super(Registry.LASER_BLOCK_TILE);
    }

    // TODO:cache the blockposition on a list for faster accsssing
    // this is already server only
    public void updateBeam() {
        if (this.canEmit()) {
            BlockPos p = this.pos;
            Direction dir = this.getDirection();
            int i = 0;
            boolean noblockfound = false;
            for (i = 0; i <= MAXLENGHT; i++) {
                p = this.pos.offset(dir, i + 1);
                BlockState state = this.world.getBlockState(p);
                if (state.getOpacity(this.world, p) < 15)
                    continue;
                if (state.isSolidSide(world, p, dir.getOpposite())) {
                    noblockfound = false;
                    break;
                }
            }
            if (this.lenght != i) {
                this.lenght = i;
                this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
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
            BlockState state = this.world.getBlockState(this.endpos);
            if (state.getBlock() instanceof LaserBlock
                    && state.get(LaserBlock.RECEIVING) != MathHelper.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)
                    && state.get(LaserBlock.FACING) == this.getBlockState().get(LaserBlock.FACING).getOpposite()) {
                this.world.setBlockState(this.endpos, state.with(LaserBlock.RECEIVING, MathHelper.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)), 3);
            }
        }
    }// TODO:o check if null

    public void turnOffReceivingLaser() {
        if (endpos != null) {
            BlockState state = this.world.getBlockState(this.endpos);
            if (state.getBlock() instanceof LaserBlock && state.get(LaserBlock.RECEIVING) != 0
                    && state.get(LaserBlock.FACING) == this.getBlockState().get(LaserBlock.FACING).getOpposite()) {
                this.world.setBlockState(this.endpos, state.with(LaserBlock.RECEIVING, 0), 3);
            }
        }
    }

    public boolean canEmit() {
        return this.isPowered() && !this.isReceiving();
    }

    public boolean isReceiving() {
        return this.getBlockState().get(LaserBlock.RECEIVING) > 0;
    }

    public boolean isPowered() {
        return this.getBlockState().get(LaserBlock.POWERED);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos.add(-20,-20,-20), this.pos.add(20,20,20));
        //return CommonUtil.getDirectionBB(this.pos, this.getDirection(), this.lenght);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 128;
    }

    @Override
    public void tick() {
        if (this.world.isRemote()) {
            if (this.offset == -1)
                this.offset = (new Random(this.getPos().toLong())).nextFloat() * (float) Math.PI * 2f;
            this.prevWidth = this.width;
            float angle = this.offset + (this.getWorld().getGameTime()%24000) / 50f;
            this.width = MathHelper.sin(angle % (float) Math.PI * 2f);
        } else if (this.world != null && this.world.getGameTime() % 20L == 0L) {
            this.updateBeam();
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.lenght = compound.getInt("Length");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("Length", this.lenght);
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
        this.updateBeam();
    }

    @Override
    public void remove() {
        super.remove();
    }

    public Direction getDirection() {
        return this.getBlockState().get(LaserBlock.FACING);
    }
}