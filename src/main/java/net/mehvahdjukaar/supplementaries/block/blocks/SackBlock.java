package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.block.tiles.SackBlockTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

public class SackBlock extends FallingBlock {

    public static final VoxelShape SHAPE_CLOSED = VoxelShapes.or(Block.box(2,0,2,14,12,14),
            Block.box(6,12,6,10,13,10),Block.box(5,13,5,11,16,11));
    public static final VoxelShape SHAPE_OPEN = VoxelShapes.or(Block.box(2,0,2,14,12,14),
            Block.box(6,12,6,10,13,10),Block.box(3,13,3,13,14,13));


    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SackBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(WATERLOGGED,false));
    }

    @Override
    public int getDustColor(BlockState state, IBlockReader reader, BlockPos pos) {
        return 0xba8f6a;
    }

    //falling block
    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(state.getBlock()!=oldState.getBlock())
            worldIn.getBlockTicks().scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OPEN,WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    //@Override
    //protected void onStartFalling(FallingBlockEntity fallingEntity) { fallingEntity.setHurtEntities(true); }

    public static boolean canFall(BlockPos pos, IWorld world){
        return (world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below()))) &&
                 pos.getY() >= 0 && !RopeBlock.isSupportingCeiling(pos.above(),world);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {

        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile te = ((SackBlockTile)tileentity);
            te.barrelTick();

            if (canFall(pos,worldIn)) {
                FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos)){
                    @Override
                    public ItemEntity spawnAtLocation(IItemProvider itemIn, int offset) {
                        ItemStack stack = new ItemStack(itemIn);
                        if(itemIn instanceof Block && ((Block) itemIn).defaultBlockState().hasTileEntity()){
                            stack.addTagElement("BlockEntityTag", this.blockData);
                        }
                        return this.spawnAtLocation(stack, (float)offset);
                    }
                    //why are values private?? I have to do this...
                    @Override
                    public boolean causeFallDamage(float distance, float damageMultiplier) {
                        int i = MathHelper.ceil(distance - 1.0F);
                        if (i > 0) {
                            List<Entity> list = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
                            DamageSource damagesource =  DamageSource.FALLING_BLOCK;
                            //half anvil damage
                            for(Entity entity : list) {
                                entity.hurt(damagesource, (float)Math.min(MathHelper.floor((float)i * 1), 20));
                            }
                        }
                        return false;
                    }

                };
                CompoundNBT com = new CompoundNBT();
                te.save(com);
                fallingblockentity.blockData = com;
                this.falling(fallingblockentity);
                worldIn.addFreshEntity(fallingblockentity);
            }

        }
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
        return new SackBlockTile();
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            return ActionResultType.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResultType.CONSUME;
        } else {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SackBlockTile) {

                player.openMenu((INamedContainerProvider) tileentity);
                PiglinTasks.angerNearbyPiglins(player, true);

                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.PASS;
            }
        }
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile sack = (SackBlockTile)tileentity;
            if (!worldIn.isClientSide && player.isCreative() && !sack.isEmpty()) {
                CompoundNBT compoundnbt = sack.saveToTag(new CompoundNBT());
                ItemStack itemstack = new ItemStack(this.getBlock());
                if (!compoundnbt.isEmpty()) {
                    itemstack.addTagElement("BlockEntityTag", compoundnbt);
                }

                if (sack.hasCustomName()) {
                    itemstack.setHoverName(sack.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                sack.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile sack = (SackBlockTile)tileentity;
            builder = builder.withDynamicDrop(CONTENTS, (context, stackConsumer) -> {
                for(int i = 0; i < sack.getContainerSize(); ++i) {
                    stackConsumer.accept(sack.getItem(i));
                }
            });
        }
        return super.getDrops(state, builder);
    }

    //pick block
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack itemstack = super.getCloneItemStack(world, pos, state);
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof SackBlockTile){
            CompoundNBT compoundnbt = ((SackBlockTile)te).saveToTag(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SackBlockTile) {
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
        if(state.getValue(OPEN))
            return SHAPE_OPEN;
        return SHAPE_CLOSED;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SackBlockTile) {
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
        TileEntity tile = worldIn.getBlockEntity(pos);

        if (tile instanceof SackBlockTile) {
            SackBlockTile sack = ((SackBlockTile) tile);
            int i = 0;
            float f = 0.0F;
            int slots = sack.getUnlockedSlots();
            for(int j = 0; j < slots; ++j) {
                ItemStack itemstack = sack.getItem(j);
                if (!itemstack.isEmpty()) {
                    f += (float)itemstack.getCount() / (float)Math.min(sack.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }
            f = f / (float)slots;
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }

}
