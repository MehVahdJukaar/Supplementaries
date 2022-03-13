package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
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
