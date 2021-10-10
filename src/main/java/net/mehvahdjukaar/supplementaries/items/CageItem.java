package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CageItem extends AbstractMobContainerItem {


    public CageItem(Block block, Properties properties) {
        super(block, properties, 0.875f, 1f, false);
    }

    @Override
    public void playCatchSound(PlayerEntity player) {
        player.level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_FALL, SoundCategory.BLOCKS, 1, 0.7f);
    }

    @Override
    public void playFailSound(PlayerEntity player) {

    }

    @Override
    public void playReleaseSound(World world, Vector3d v) {
        world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
    }



    @Override
    public boolean canItemCatch(Entity e) {
        if (ServerConfigs.cached.CAGE_AUTO_DETECT && this.canFitEntity(e)) return true;

        EntityType<?> type = e.getType();

        boolean isBaby = e instanceof LivingEntity && ((LivingEntity) e).isBaby();
        return ((ServerConfigs.cached.CAGE_ALL_BABIES && isBaby) ||
                type.is(ModTags.CAGE_CATCHABLE) ||
                (type.is(ModTags.CAGE_BABY_CATCHABLE) && isBaby));
    }






}
