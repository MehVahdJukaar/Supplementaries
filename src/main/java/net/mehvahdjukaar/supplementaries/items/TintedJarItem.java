package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class TintedJarItem extends JarItem {
    public TintedJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    public boolean canItemCatch(Entity e) {
        EntityType<?> type = e.getType();
        if (ServerConfigs.cached.JAR_AUTO_DETECT && this.canFitEntity(e)) return true;
        return type.is(ModTags.TINTED_JAR_CATCHABLE) || CapturedMobsHelper.is2DFish(type);
    }

    @Override
    public boolean isBoat(Entity e) {
        return false;
    }
}
