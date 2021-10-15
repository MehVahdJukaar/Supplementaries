package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Mth;

public class WindVaneBlockTile extends BlockEntity implements TickableBlockEntity {
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
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        float tp = (float) (Math.PI*2);
        this.offset=400*(Mth.sin((0.005f*this.worldPosition.getX())%tp) + Mth.sin((0.005f*this.worldPosition.getZ())%tp) + Mth.sin((0.005f*this.worldPosition.getY())%tp));

    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
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
            float newyaw = max_angle_1 * Mth.sin(tp * ((t * b / period_1)%360))
                    + max_angle_2 * Mth.sin(tp * ((t * b / period_2)%360));
            this.yaw = Mth.clamp(newyaw, currentyaw - 8, currentyaw + 8);
        }
    }
}