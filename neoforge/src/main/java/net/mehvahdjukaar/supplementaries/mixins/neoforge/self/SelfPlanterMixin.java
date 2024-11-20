package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TwistingVinesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlanterBlock.class)
public abstract class SelfPlanterMixin extends Block {

    protected SelfPlanterMixin(Properties arg) {
        super(arg);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }
}
