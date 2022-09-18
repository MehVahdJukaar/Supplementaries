package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightUpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GunpowderBlock.class)
public abstract class SelfGunpowderMixin extends LightUpBlock {
    public SelfGunpowderMixin(Properties arg) {
        super(arg);
    }

    // TODO fabric

    /**
     * Called upon the block being destroyed by an explosion
     */
    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if (!world.isClientSide && this.canSurvive(state, world, pos)) {
            this.lightUp(null, state, pos, world, FireSourceType.FLAMING_ARROW);
        } else {
            super.onBlockExploded(state, world, pos, explosion);
        }
    }
}
