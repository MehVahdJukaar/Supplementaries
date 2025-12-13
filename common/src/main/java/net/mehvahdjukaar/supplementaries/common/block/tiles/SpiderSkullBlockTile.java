package net.mehvahdjukaar.supplementaries.common.block.tiles;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpiderSkullBlockTile extends SkullBlockEntity {
    public SpiderSkullBlockTile(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.SPIDER_SKULL_TILE.get();
    }

    //thanks mojank
    @PlatformOnly(value = PlatformOnly.FABRIC)
    @Override
    public boolean isValidBlockState(BlockState blockState) {
        return this.getType().isValid(blockState);
    }

    @Override
    public @Nullable ResourceLocation getNoteBlockSound() {
        return ModSounds.IMITATE_SPIDER.getId();
    }
}
