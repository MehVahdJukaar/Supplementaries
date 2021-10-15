package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.block.tiles.SackBlockTile;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ItemLike;

public class SackBlock extends FallingBlock {

    public static final VoxelShape SHAPE_CLOSED = Shapes.or(Block.box(2,0,2,14,12,14),
            Block.box(6,12,6,10,13,10),Block.box(5,13,5,11,16,11));
    public static final VoxelShape SHAPE_OPEN = Shapes.or(Block.box(2,0,2,14,12,14),
            Block.box(6,12,6,10,13,10),Block.box(3,13,3,13,14,13));


    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(WATERLOGGED,false));
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return 0xba8f6a;
    }

    //falling block
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(state.getBlock()!=oldState.getBlock())
            worldIn.getBlockTicks().scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN,WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    //@Override
    //protected void onStartFalling(FallingBlockEntity fallingEntity) { fallingEntity.setHurtEntities(true); }

    public static boolean canFall(BlockPos pos, LevelAccessor world){
        return (world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below()))) &&
                 pos.getY() >= 0 && !RopeBlock.isSupportingCeiling(pos.above(),world);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {

        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile te = ((SackBlockTile)tileentity);
            te.barrelTick();

            if (canFall(pos,worldIn)) {
                FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos)){
                    @Override
                    public ItemEntity spawnAtLocation(ItemLike itemIn, int offset) {
                        ItemStack stack = new ItemStack(itemIn);
                        if(itemIn instanceof Block && ((Block) itemIn).defaultBlockState().hasTileEntity()){
                            stack.addTagElement("BlockEntityTag", this.blockData);
                        }
                        return this.spawnAtLocation(stack, (float)offset);
                    }
                    //why are values private?? I have to do this...
                    @Override
                    public boolean causeFallDamage(float distance, float damageMultiplier) {
                        int i = Mth.ceil(distance - 1.0F);
                        if (i > 0) {
                            List<Entity> list = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
                            DamageSource damagesource =  DamageSource.FALLING_BLOCK;
                            //half anvil damage
                            for(Entity entity : list) {
                                entity.hurt(damagesource, (float)Math.min(Mth.floor((float)i * 1), 20));
                            }
                        }
                        return false;
                    }

                };
                CompoundTag com = new CompoundTag();
                te.save(com);
                fallingblockentity.blockData = com;
                this.falling(fallingblockentity);
                worldIn.addFreshEntity(fallingblockentity);
            }

        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new SackBlockTile();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SackBlockTile) {

                player.openMenu((MenuProvider) tileentity);
                PiglinAi.angerNearbyPiglins(player, true);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SackBlockTile) {
            SackBlockTile sack = (SackBlockTile)tileentity;
            if (!worldIn.isClientSide && player.isCreative() && !sack.isEmpty()) {
                CompoundTag compoundnbt = sack.saveToTag(new CompoundTag());
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
        BlockEntity tileentity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
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

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getPickBlock(state, target, world, pos, player);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof SackBlockTile){
            CompoundTag compoundnbt = ((SackBlockTile)te).saveToTag(new CompoundTag());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SackBlockTile) {
                ((BaseContainerBlockEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if(state.getValue(OPEN))
            return SHAPE_OPEN;
        return SHAPE_CLOSED;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
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
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity tile = worldIn.getBlockEntity(pos);

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
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity instanceof MenuProvider ? (MenuProvider)tileentity : null;
    }

}
