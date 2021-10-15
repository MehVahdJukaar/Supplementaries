package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.LaserBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.Constants;

import java.util.Random;


public class LaserBlockTile extends BlockEntity implements TickableBlockEntity {
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
                    && state.getValue(LaserBlock.RECEIVING) != Mth.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)
                    && state.getValue(LaserBlock.FACING) == this.getBlockState().getValue(LaserBlock.FACING).getOpposite()) {
                this.level.setBlock(this.endpos, state.setValue(LaserBlock.RECEIVING, Mth.clamp(MAXLENGHT + 1 - this.lenght, 0, 15)), 3);
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
    public AABB getRenderBoundingBox() {
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
            this.width = Mth.sin(angle % (float) Math.PI * 2f);
        } else if (this.level != null && this.level.getGameTime() % 20L == 0L) {
            this.updateBeam();
        }
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.lenght = compound.getInt("Length");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putInt("Length", this.lenght);
        return compound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
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