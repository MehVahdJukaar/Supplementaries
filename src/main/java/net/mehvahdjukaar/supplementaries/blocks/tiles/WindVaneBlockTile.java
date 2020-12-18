package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
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
    private float offset = 0;

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
        float tp = (float) (Math.PI*2);
        this.offset=400*(MathHelper.sin((0.005f*this.pos.getX())%tp) + MathHelper.sin((0.005f*this.pos.getZ())%tp) + MathHelper.sin((0.005f*this.pos.getY())%tp));

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
            float tp = (float) (2f*Math.PI);
            //float offset = 3f * (MathHelper.sin(0.1f*this.pos.getX()) + 0.1f*MathHelper.sin(this.pos.getZ()) + 0.1f*MathHelper.sin(this.pos.getY()));
            float t = this.world.getGameTime()%24000 + this.offset;
            float b = (float) Math.max(1,(power * ClientConfigs.cached.WIND_VANE_POWER_SCALING));
            float max_angle_1 = (float) ClientConfigs.cached.WIND_VANE_ANGLE_1;
            float max_angle_2 = (float) ClientConfigs.cached.WIND_VANE_ANGLE_2;
            float period_1 = (float) ClientConfigs.cached.WIND_VANE_PERIOD_1;
            float period_2 = (float) ClientConfigs.cached.WIND_VANE_PERIOD_2;
            float newyaw = max_angle_1 * MathHelper.sin(tp * ((t * b / period_1)%360))
                    + max_angle_2 * MathHelper.sin(tp * ((t * b / period_2)%360));
            this.yaw = MathHelper.clamp(newyaw, currentyaw - 8, currentyaw + 8);
        }
    }
}