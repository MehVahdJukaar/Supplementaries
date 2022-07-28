package net.mehvahdjukaar.supplementaries.common.block.blocks;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
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
            return tile.fluidHolder.getParticleColor(world, pos);
        }
        return 0xffffff;
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        int color = getJarLiquidColor(pos, world);
        if (color == -1) return null;
        float r = (float) ((color >> 16 & 255)) / 255.0F;
        float g = (float) ((color >> 8 & 255)) / 255.0F;
        float b = (float) ((color & 255)) / 255.0F;
        return new float[]{r, g, b};
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof JarBlockTile tile && tile.isAccessibleBy(player)) {
            // make te do the work
            if (tile.handleInteraction(player, handIn, worldIn, pos)) {
                if (!worldIn.isClientSide()) {
                    tile.setChanged();
                }
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
            if(CommonConfigs.Blocks.JAR_CAPTURE.get()) {
                return tile.mobContainer.onInteract(worldIn, pos, player, handIn);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof JarBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            BlockUtil.addOptionalOwnership(placer, tile);
        }
    }

    //TODO: improve
    public ItemStack getJarItem(JarBlockTile te) {
        ItemStack returnStack = new ItemStack(this);

        if (te.hasContent()) {
            CompoundTag compoundTag = te.saveWithoutMetadata();
            //hax
            if (compoundTag.contains("Owner")) compoundTag.remove("Owner");
            if (!compoundTag.isEmpty()) {
                returnStack.addTagElement("BlockEntityTag", compoundTag);
            }
        }
        if (te.hasCustomName()) {
            returnStack.setHoverName(te.getCustomName());
        }
        return returnStack;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JarBlockTile tile) {
            ItemStack itemstack = this.getJarItem(tile);
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof JarBlockTile tile) {
            return this.getJarItem(tile);
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
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
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
            else if (!tile.fluidHolder.isEmpty()) {
                return tile.fluidHolder.getComparatorOutput();
            } else if (!tile.mobContainer.isEmpty()) return 15;
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
        return BlockUtil.getTicker(pBlockEntityType, ModRegistry.JAR_TILE.get(), JarBlockTile::tick);
    }

}