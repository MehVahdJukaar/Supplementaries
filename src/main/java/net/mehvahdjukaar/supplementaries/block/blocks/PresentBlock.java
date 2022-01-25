package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IColored;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PresentBlock extends WaterBlock implements IColored {

    public static final BooleanProperty PACKED = BlockProperties.PACKED;

    private final DyeColor color;

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    public static final VoxelShape SHAPE_OPEN = Block.box(2, 0, 2, 14, 10, 14);
    public static final VoxelShape SHAPE_CLOSED = VoxelShapes.or(SHAPE_OPEN, Block.box(1, 10, 1, 15, 14, 15));

    public PresentBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(PACKED, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PACKED, WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PresentBlockTile();
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            return ActionResultType.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResultType.CONSUME;
        } else {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof PresentBlockTile) {


                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileentity, tileentity.getBlockPos());
                //player.openMenu((INamedContainerProvider) tileentity);
                PiglinTasks.angerNearbyPiglins(player, true);

                return ActionResultType.CONSUME;

            }
        }
        return ActionResultType.PASS;
    }


    public ItemStack getPresentItem(PresentBlockTile te) {
        CompoundNBT compoundnbt = te.save(new CompoundNBT());
        ItemStack itemstack = new ItemStack(this.getBlock());
        if (!compoundnbt.isEmpty()) {
            itemstack.addTagElement("BlockEntityTag", compoundnbt);
        }

        if (te.hasCustomName()) {
            itemstack.setHoverName(te.getCustomName());
        }
        return itemstack;
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof PresentBlockTile) {
            PresentBlockTile te = (PresentBlockTile) tileentity;
            if (!worldIn.isClientSide && player.isCreative() && !te.isEmpty()) {
                ItemStack itemstack = this.getPresentItem(te);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                te.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof PresentBlockTile) {
            PresentBlockTile te = (PresentBlockTile) tileentity;
            ItemStack itemstack = this.getPresentItem(te);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack itemstack = super.getPickBlock(state, target, world, pos, player);
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PresentBlockTile) {
            return getPresentItem((PresentBlockTile) te);
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof PresentBlockTile) {
                ((LockableTileEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (state.getValue(PACKED))
            return SHAPE_CLOSED;
        return SHAPE_OPEN;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof PresentBlockTile) {
                worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider) tileentity : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        CompoundNBT tag = context.getItemInHand().getTag();
        if (tag != null && tag.contains("BlockEntityTag")) {
            CompoundNBT t = tag.getCompound("BlockEntityTag");
            if (t.contains("Items")) state = state.setValue(PACKED, true);
        }
        return state;
    }
}
