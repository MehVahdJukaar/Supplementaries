package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.Rune;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EndLampBlock extends Block {

    public static final EnumProperty<Rune> RUNE = BlockProperties.RUNE;

    public EndLampBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RUNE, Rune.A));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(RUNE, Rune.values()[RANDOM.nextInt(Rune.values().length)]);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        if (!worldIn.isClientSide) {
            int inc = player.isShiftKeyDown() ? -1 : 1;
            worldIn.setBlockAndUpdate(pos, state.setValue(RUNE, Rune.values()[(state.getValue(RUNE).ordinal() + inc) %  Rune.values().length]));
            worldIn.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.25F, 1.7f);

            return ActionResultType.CONSUME;
        }
        return ActionResultType.SUCCESS;

    }
}
