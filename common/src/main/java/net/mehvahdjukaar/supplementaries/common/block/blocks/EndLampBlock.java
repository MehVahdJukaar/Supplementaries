package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Rune;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EndLampBlock extends Block {

    public static final EnumProperty<Rune> RUNE = ModBlockProperties.RUNE;

    public EndLampBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RUNE, Rune.A));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        RandomSource r = RandomSource.create(context.getClickedPos().asLong());
        return this.defaultBlockState().setValue(RUNE, Rune.values()[r.nextInt(Rune.values().length)]);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            int inc = player.isShiftKeyDown() ? -1 : 1;
            level.setBlockAndUpdate(pos, state.setValue(RUNE,
                    Rune.values()[(state.getValue(RUNE).ordinal() + inc + Rune.values().length) % Rune.values().length]));
            level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.25F, 1.7f);

            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;

    }
}
