package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.common.block.blocks.MimicBlock;
import net.mehvahdjukaar.supplementaries.common.block.IBlockHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MimicBlock.class)
public abstract class SelfMimicMixin extends Block {

    public SelfMimicMixin(Properties arg) {
        super(arg);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (world.getBlockEntity(pos) instanceof IBlockHolder tile) {
            BlockState mimicState = tile.getHeldBlock();
            if (!mimicState.isAir()) {
                return mimicState.getExplosionResistance(world, pos, explosion);
            }
        }
        return 2;
    }
}
