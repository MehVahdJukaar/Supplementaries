package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class WindVaneBlockTile extends TileEntity implements ITickableTileEntity {
    public float yaw = 0;
    public float prevYaw = 0;

    public WindVaneBlockTile() {
        super(Registry.WIND_VANE_TILE);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
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

    @Override
    public void tick() {
        float currentyaw = this.yaw;
        this.prevYaw = currentyaw;
        if(this.world == null)return;
        if (!this.world.isRemote()) {
            if (this.world != null && this.world.getGameTime() % 20L == 0L) {
                BlockState blockstate = this.getBlockState();
                Block block = blockstate.getBlock();
                if (block instanceof WindVaneBlock) {
                    WindVaneBlock.updatePower(blockstate, this.world, this.pos);
                }
            }
        } else {
            int power = this.getBlockState().get(WindVaneBlock.POWER);
            // TODO:cache some of this maybe?
            float hightoffset = 0;// (this.pos.getY()-64)/192f;
            float offset = 3f * (MathHelper.sin(this.pos.getX()) + MathHelper.sin(this.pos.getZ()) + MathHelper.sin(this.pos.getY()));
            float i = this.world.getDayTime() + offset;
            float b = (power + hightoffset) * 2f;
            float newyaw = 30f * MathHelper.sin(i * (1f + b) / 60f) + 10f * MathHelper.sin(i * (1f + b) / 20f);
            this.yaw = MathHelper.clamp(newyaw, currentyaw - 8, currentyaw + 8);
        }
    }
}