package net.mehvahdjukaar.supplementaries.compat.botania;

import net.minecraft.tileentity.TileEntityType;
import vazkii.botania.common.block.tile.TileTinyPotato;

import javax.annotation.Nonnull;

public class TaterInAJarBlockTile extends TileTinyPotato {

    public TaterInAJarBlockTile() {
        super();
        //try {
        //    Field f = ObfuscationReflectionHelper.findField(TileEntity.class, "type");
        //    f.setAccessible(true);
        //    f.set(this, BotaniaCompatRegistry.TATER_IN_A_JAR_TILE.get());
        //    f.setAccessible(false);
        // } catch (Exception ignored) {
        //}
    }

    @Nonnull
    public TileEntityType<TaterInAJarBlockTile> getType() {
        return BotaniaCompatRegistry.TATER_IN_A_JAR_TILE.get();
    }
}
