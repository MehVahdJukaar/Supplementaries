package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;


public class MobJarBlockTile extends TileEntity implements ITickableTileEntity {
    public Entity mob = null;
    public CompoundNBT entityData = null;
    public boolean entityChanged = false;
    public MobJarBlockTile() {
        super(Registry.MOB_JAR_TILE);
    }

    public void updateMob(){

        Entity entity  = EntityType.loadEntityAndExecute(this.entityData, this.world, o -> o);
        if(entity==null && this.entityData.contains("id")){
            boolean flag = this.entityData.get("id").getString().equals("minecraft:bee");
            if(flag) entity = new BeeEntity(EntityType.BEE, this.world);
        }
        this.mob = entity;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if(compound.contains("jar_mob")){
            this.entityData = compound.getCompound("jar_mob");
            this.entityChanged = true;
        }

    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(this.entityData!=null)
            compound.put("jar_mob", this.entityData);
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

    public void tick() {


        if(this.entityChanged&& this.entityData!=null){
            this.updateMob();
            this.entityChanged=false;
        }
        //for client side animation
        if(this.mob!=null)
            this.mob.ticksExisted++;
    }
}