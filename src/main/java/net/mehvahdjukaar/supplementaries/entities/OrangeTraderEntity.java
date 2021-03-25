package net.mehvahdjukaar.supplementaries.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class OrangeTraderEntity extends VillagerEntity {
    public OrangeTraderEntity(EntityType<? extends VillagerEntity> type, World worldIn) {
        super(type, worldIn);
    }
    /*
    public OrangeTraderEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        super(Registry.ROPE_ARROW.get(), world);
    }*/

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
