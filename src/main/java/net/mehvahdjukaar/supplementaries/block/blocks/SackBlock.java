package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
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
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
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
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

public class SackBlock extends FallingBlock {
    public static final VoxelShape SHAPE = Block.makeCuboidShape(2,0,2,14,12,14);
    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SackBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(OPEN, false));
    }

    public int getDustColor(BlockState state, IBlockReader reader, BlockPos pos) {
        return -5671355;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OPEN,WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, flag);
    }

    //@Override
    //protected void onStartFalling(FallingBlockEntity fallingEntity) { fallingEntity.setHurtEntities(true); }

    public boolean canFall(BlockPos pos, World world){
        return (world.isAirBlock(pos.down()) || canFallThrough(world.getBlockState(pos.down()))) &&
                !hasEnoughSolidSide(world, pos.up(), Direction.DOWN) && pos.getY() >= 0 &&
                !ServerConfigs.cached.SACK_WHITELIST.contains(world.getBlockState(pos.up()).getBlock().getRegistryName().toString());
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile te = ((SackBlockTile)tileentity);
            te.barrelTick();

            if (this.canFall(pos,worldIn)) {
                FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos)){
                    @Override
                    public ItemEntity entityDropItem(IItemProvider itemIn, int offset) {
                        ItemStack stack = new ItemStack(itemIn);
                        if(itemIn instanceof Block && ((Block) itemIn).getDefaultState().hasTileEntity()){
                            stack.setTagInfo("BlockEntityTag", this.tileEntityData);
                        }
                        return this.entityDropItem(stack, (float)offset);
                    }
                    //why are values private?? I have to do this...
                    @Override
                    public boolean onLivingFall(float distance, float damageMultiplier) {
                        int i = MathHelper.ceil(distance - 1.0F);
                        if (i > 0) {
                            List<Entity> list = Lists.newArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox()));
                            DamageSource damagesource =  DamageSource.FALLING_BLOCK;
                            //half anvil damage
                            for(Entity entity : list) {
                                entity.attackEntityFrom(damagesource, (float)Math.min(MathHelper.floor((float)i * 1), 20));
                            }
                        }
                        return false;
                    }

                };
                CompoundNBT com = new CompoundNBT();
                te.write(com);
                fallingblockentity.tileEntityData = com;
                this.onStartFalling(fallingblockentity);
                worldIn.addEntity(fallingblockentity);
            }

        }
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResultType.CONSUME;
        } else {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SackBlockTile) {

                player.openContainer((INamedContainerProvider) tileentity);
                PiglinTasks.func_234478_a_(player, true);

                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.PASS;
            }
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile sack = (SackBlockTile)tileentity;
            if (!worldIn.isRemote && player.isCreative() && !sack.isEmpty()) {
                CompoundNBT compoundnbt = sack.saveToNbt(new CompoundNBT());
                ItemStack itemstack = new ItemStack(this.getBlock());
                if (!compoundnbt.isEmpty()) {
                    itemstack.setTagInfo("BlockEntityTag", compoundnbt);
                }

                if (sack.hasCustomName()) {
                    itemstack.setDisplayName(sack.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            } else {
                sack.fillWithLoot(player);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile sack = (SackBlockTile)tileentity;
            builder = builder.withDynamicDrop(CONTENTS, (context, stackConsumer) -> {
                for(int i = 0; i < sack.getSizeInventory(); ++i) {
                    stackConsumer.accept(sack.getStackInSlot(i));
                }
            });
        }
        return super.getDrops(state, builder);
    }

    //pick block
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack itemstack = super.getItem(world, pos, state);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SackBlockTile){
            CompoundNBT compoundnbt = ((SackBlockTile)te).saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.setTagInfo("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SackBlockTile) {
                ((SackBlockTile)tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof ShulkerBoxTileEntity) {
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory)worldIn.getTileEntity(pos));
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }




}
