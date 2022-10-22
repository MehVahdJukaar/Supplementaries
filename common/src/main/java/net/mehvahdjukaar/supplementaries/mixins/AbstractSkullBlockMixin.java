package net.mehvahdjukaar.supplementaries.mixins;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSkullBlock.class)
public abstract class AbstractSkullBlockMixin extends BaseEntityBlock {

    protected AbstractSkullBlockMixin(Properties properties) {
        super(properties);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return SoundType.BONE_BLOCK;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.BONE_BLOCK;
    }

    //fixing vanilla bug don't mind me. Should not be invisible, so it can be called by render single block
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}