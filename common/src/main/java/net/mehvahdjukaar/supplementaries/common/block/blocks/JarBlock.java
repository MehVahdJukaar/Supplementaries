package net.mehvahdjukaar.supplementaries.common.block.blocks;


import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class JarBlock extends WaterBlock implements EntityBlock {
    public static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 14, 13),
            Block.box(5, 14, 5, 11, 16, 11));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL_0_15;

    public JarBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(JarBlock.LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    //check if it only gets called client side
    public int getJarLiquidColor(BlockPos pos, LevelReader world) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof JarBlockTile tile) {
            return tile.getSoftFluidTank().getCachedParticleColor(world, pos);
        }
        return 0xffffff;
    }

    @ForgeOverride
    public float @Nullable [] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        int color = getJarLiquidColor(pos, world);
        if (color == -1) return null;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        return new float[]{r, g, b};
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof JarBlockTile tile ) {
            // make te do the work
            if (tile.handleInteraction(player, hand, level, pos)) {
                if (!level.isClientSide()) {
                    tile.setChanged();
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (CommonConfigs.Functional.JAR_CAPTURE.get()) {
                return tile.getMobContainer().onInteract(level, pos, player, hand, stack);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JarBlockTile tile) {
            ItemStack itemstack = Utils.saveTileToItem(tile);
            //TODO: replace with loot table
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof JarBlockTile tile) {
            return Utils.saveTileToItem(tile);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    // end shoulker box code
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        Preconditions.checkArgument(pState.is(this), "Invalid blockstate for block "+this+ " HOW? who passed it?");
        return new JarBlockTile(pPos, pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof JarBlockTile tile) {
            if (!tile.isEmpty())
                return AbstractContainerMenu.getRedstoneSignalFromContainer(tile);
            else if (!tile.getSoftFluidTank().isEmpty()) {
                return tile.getSoftFluidTank().getComparatorOutput();
            } else if (!tile.getMobContainer().isEmpty()) return 15;
        }
        return 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.JAR_TILE.get(), JarBlockTile::tick);
    }

}