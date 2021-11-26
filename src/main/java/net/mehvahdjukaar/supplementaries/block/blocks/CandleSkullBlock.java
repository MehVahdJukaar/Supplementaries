package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.supplementaries.block.tiles.CandleSkullBlockTile;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class CandleSkullBlock extends AbstractCandleBlock implements EntityBlock {

    private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectMap<List<Vec3>> map = new Int2ObjectOpenHashMap<>();
        map.defaultReturnValue(ImmutableList.of());
        map.put(1, ImmutableList.of(new Vec3(0.5D, 0.5 + 0.5D, 0.5D)));
        map.put(2, ImmutableList.of(new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.625D, 0.5 + 0.5D, 0.44D)));
        map.put(3, ImmutableList.of(new Vec3(0.5D, 0.5 + 0.313D, 0.625D), new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.56D, 0.5 + 0.5D, 0.44D)));
        map.put(4, ImmutableList.of(new Vec3(0.44D, 0.5 + 0.313D, 0.56D), new Vec3(0.625D, 0.5 + 0.44D, 0.56D), new Vec3(0.375D, 0.5 + 0.44D, 0.375D), new Vec3(0.56D, 0.5 + 0.5D, 0.375D)));
        return Int2ObjectMaps.unmodifiable(map);
    });

    protected static final VoxelShape SHAPE = Shapes.or(Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(7.0D, 8.0D, 7.0D, 9.0D, 14.0D, 9.0D));

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;

    public CandleSkullBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CANDLES, 1)
                .setValue(ROTATION, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(CANDLES, LIT, ROTATION);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CandleSkullBlockTile(pPos, pState);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CandleSkullBlockTile tile) {
            List<ItemStack> loot = tile.getCandle().getDrops(builder);
            loot.add(tile.getSkullItem());
            return loot;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return CandleBlock.LIGHT_EMISSION.applyAsInt(state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        return PARTICLE_OFFSETS.get(pState.getValue(CANDLES).intValue());
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.getAbilities().mayBuild && pPlayer.getItemInHand(pHand).isEmpty() && pState.getValue(LIT)) {
            extinguish(pPlayer, pState, pLevel, pPos);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }


}
