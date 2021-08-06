package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.fluids.ISoftFluidConsumer;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BambooSpikesBlock extends WaterBlock implements ISoftFluidConsumer {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    protected static final VoxelShape SHAPE_UP = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    protected static final VoxelShape SHAPE_DOWN = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TIPPED = BlockProperties.TIPPED;


    public BambooSpikesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED,false).setValue(TIPPED,false));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    //this could be improved
    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof BambooSpikesBlockTile){
            CompoundNBT com = stack.getTag();
            if(com!=null){
                Potion p = PotionUtils.getPotion(stack);
                if(p != Potions.EMPTY && com.contains("Damage")){
                    ((BambooSpikesBlockTile) te).potion = p;
                    ((BambooSpikesBlockTile) te).setMissingCharges(com.getInt("Damage"));
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        CompoundNBT com = context.getItemInHand().getTag();
        int charges = com!=null?context.getItemInHand().getMaxDamage()-com.getInt("Damage"):0;
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;;
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED,flag)
                .setValue(TIPPED, charges!=0 && PotionUtils.getPotion(com)!=Potions.EMPTY);
    }

    public ItemStack getSpikeItem(TileEntity te){
        if(te instanceof BambooSpikesBlockTile) {
            return ((BambooSpikesBlockTile) te).getSpikeItem();
        }
        return new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> list = new ArrayList<>();
        list.add(this.getSpikeItem(builder.getOptionalParameter(LootParameters.BLOCK_ENTITY)));
        return list;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)){
            default:
            case DOWN:
                return SHAPE_DOWN;
            case UP:
                return SHAPE_UP;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
        }
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.block();
    }

    //TODO: fix pathfinding

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).isCreative())return;
        if(entityIn instanceof LivingEntity && entityIn.isAlive()) {
            boolean up = state.getValue(FACING) == Direction.UP;
            double vy = up ? 0.45 : 0.95;
            entityIn.makeStuckInBlock(state, new Vector3d(0.95D, vy, 0.95D));
            if(!worldIn.isClientSide) {
                if(up && entityIn instanceof PlayerEntity && entityIn.isShiftKeyDown())return;
                float damage = entityIn.getY() > (pos.getY() + 0.0625) ? 2 : 1;
                entityIn.hurt(CommonUtil.SPIKE_DAMAGE, damage);
                if(state.getValue(TIPPED)) {
                    TileEntity te = worldIn.getBlockEntity(pos);
                    if (te instanceof BambooSpikesBlockTile) {
                        if(((BambooSpikesBlockTile)te).interactWithEntity(((LivingEntity) entityIn),worldIn)){
                            worldIn.setBlock(pos,state.setValue(BambooSpikesBlock.TIPPED,false),3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.DAMAGE_OTHER;
    }

    public static boolean tryAddingPotion(BlockState state, IWorld world, BlockPos pos, ItemStack stack){
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile) {
            if (((BambooSpikesBlockTile) te).tryApplyPotion(PotionUtils.getPotion(stack))) {
                world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
                world.setBlock(pos,state.setValue(TIPPED,true),3);
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!tippedEnabled.get())return ActionResultType.PASS;
        ItemStack stack = player.getItemInHand(handIn);

        if(stack.getItem() instanceof LingeringPotionItem) {
            if(tryAddingPotion(state,worldIn,pos,stack)){
                if (!player.isCreative())
                    player.setItemInHand(handIn, DrinkHelper.createFilledResult(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            }
            return ActionResultType.sidedSuccess(worldIn.isClientSide);
        }
        return ActionResultType.PASS;
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED,TIPPED);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return this.getSpikeItem(world.getBlockEntity(pos));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BambooSpikesBlockTile();
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if(0.01>random.nextFloat() && state.getValue(TIPPED)){
            TileEntity te = world.getBlockEntity(pos);
            if(te instanceof BambooSpikesBlockTile) {
                ((BambooSpikesBlockTile) te).makeParticle();
            }
        }
    }

    public Lazy<Boolean> tippedEnabled = Lazy.of(()->RegistryConfigs.reg.TIPPED_SPIKES_ENABLED.get());

    @Override
    public boolean tryAcceptingFluid(World world, BlockState state, BlockPos pos, SoftFluid f, @Nullable CompoundNBT nbt, int amount) {
        if(!tippedEnabled.get())return false;
        if(f == SoftFluidRegistry.POTION && nbt != null && !state.getValue(TIPPED) && nbt.getString("PotionType").equals("Lingering")){
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof BambooSpikesBlockTile) {
                if (((BambooSpikesBlockTile) te).tryApplyPotion(PotionUtils.getPotion(nbt))) {
                    world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    world.setBlock(pos, state.setValue(TIPPED, true), 3);
                    return true;
                }
            }
        }
        return false;
    }
}