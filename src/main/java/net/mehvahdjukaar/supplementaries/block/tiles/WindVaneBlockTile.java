package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
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
        super(ModRegistry.WIND_VANE_TILE.get());

    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        float tp = (float) (Math.PI*2);
        this.offset=400*(MathHelper.sin((0.005f*this.worldPosition.getX())%tp) + MathHelper.sin((0.005f*this.worldPosition.getZ())%tp) + MathHelper.sin((0.005f*this.worldPosition.getY())%tp));

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
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
    }

    @Override
    public void tick() {

        float currentyaw = this.yaw;
        this.prevYaw = currentyaw;
        if(this.level == null)return;
        if (!this.level.isClientSide()) {
            if (this.level != null && this.level.getGameTime() % 20L == 0L) {
                BlockState blockstate = this.getBlockState();
                Block block = blockstate.getBlock();
                if (block instanceof WindVaneBlock) {
                    WindVaneBlock.updatePower(blockstate, this.level, this.worldPosition);
                }
            }
        } else {
            int power = this.getBlockState().getValue(WindVaneBlock.POWER);
            // TODO:cache some of this maybe?
            float tp = (float) (2f*Math.PI);
            //float offset = 3f * (MathHelper.sin(0.1f*this.pos.getX()) + 0.1f*MathHelper.sin(this.pos.getZ()) + 0.1f*MathHelper.sin(this.pos.getY()));
            float t = this.level.getGameTime()%24000 + this.offset;
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