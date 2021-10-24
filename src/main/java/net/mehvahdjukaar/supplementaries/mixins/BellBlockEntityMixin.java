package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.IBellConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

import net.mehvahdjukaar.supplementaries.block.util.IBellConnections.BellConnection;

@Mixin(BellBlockEntity.class)
public abstract class BellBlockEntityMixin extends BlockEntity  implements IBellConnections {
    public BellConnection connection = BellConnection.NONE;

    public BellBlockEntityMixin(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Override
    public BellConnection getConnected() {
        return connection;
    }

    @Override
    public void setConnected(BellConnection con) {
        this.connection=con;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        //not needed but since I keep getting reports lets do this
        try {
            if (this.connection != null)
                compound.putInt("Connection", this.connection.ordinal());
        }catch (Exception ignored){}
        return compound;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        try {
            if(compound.contains("Connection"))
                this.connection = BellConnection.values()[compound.getInt("Connection")];
        }catch (Exception ignored){
            this.connection = BellConnection.NONE;
        }
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
        this.load(pkt.getTag());
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition);
    }
}