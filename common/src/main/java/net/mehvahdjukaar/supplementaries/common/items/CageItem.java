package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.CageItemRenderer;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class CageItem extends AbstractMobContainerItem implements ICustomItemRendererProvider {


    public CageItem(Block block, Properties properties) {
        super(block, properties, 0.875f, 1f, false);
    }

    @Override
    public void playCatchSound(Player player) {
        player.level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_FALL, SoundSource.BLOCKS, 1, 0.7f);
    }

    @Override
    public void playFailSound(Player player) {
    }

    @Override
    public void playReleaseSound(Level world, Vec3 v) {
        world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1, 0.05f);
    }

    @Override
    public boolean canItemCatch(Entity e) {
        if (ServerConfigs.Blocks.CAGE_AUTO_DETECT.get() && this.canFitEntity(e)) return true;

        EntityType<?> type = e.getType();

        boolean isBaby = e instanceof LivingEntity le && le.isBaby();
        return ((ServerConfigs.Blocks.CAGE_ALL_BABIES.get() && isBaby) ||
                type.is(ModTags.CAGE_CATCHABLE) ||
                (type.is(ModTags.CAGE_BABY_CATCHABLE) && isBaby));
    }


    @Override
    public ItemStackRenderer createRenderer() {
        return new CageItemRenderer();
    }
}
