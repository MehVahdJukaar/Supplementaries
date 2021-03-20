package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Random;

public class FaucetBlock extends Block implements IWaterLoggable{
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.6875D, 0.3125D, 1D, 0.3125D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.3125D, 0.3125D, 0D, 0.6875D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(1D, 0.3125D, 0.3125D, 0.3125D, 0.9375D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0D, 0.3125D, 0.6875D, 0.6875D, 0.9375D, 0.3125D);
    protected static final VoxelShape SHAPE_NORTH_JAR = VoxelShapes.create(0.6875D, 0, 1D, 0.3125D, 0.625D, 0.3125D);
    protected static final VoxelShape SHAPE_SOUTH_JAR = VoxelShapes.create(0.3125D, 0, 0D, 0.6875D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_WEST_JAR = VoxelShapes.create(1D, 0, 0.3125D, 0.3125D, 0.625D, 0.6875D);
    protected static final VoxelShape SHAPE_EAST_JAR = VoxelShapes.create(0D, 0, 0.6875D, 0.6875D, 0.625D, 0.3125D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_WATER = BlockProperties.HAS_WATER;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public static final BooleanProperty HAS_JAR = BlockProperties.HAS_JAR;
    public static final BooleanProperty EXTENDED = BlockStateProperties.ATTACHED; //glass extension
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public FaucetBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HAS_JAR, false).with(FACING, Direction.NORTH)
                .with(ENABLED, false).with(EXTENDED, false).with(POWERED, false)
                .with(HAS_WATER, false).with(WATERLOGGED,false).with(LIGHT_LEVEL,0));
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (state.get(HAS_JAR)) {
            switch (state.get(FACING)) {
                case UP :
                case DOWN :
                case NORTH :
                default :
                    return SHAPE_NORTH_JAR;
                case SOUTH :
                    return SHAPE_SOUTH_JAR;
                case EAST :
                    return SHAPE_EAST_JAR;
                case WEST :
                    return SHAPE_WEST_JAR;
            }
        } else {
            switch (state.get(FACING)) {
                case UP :
                case DOWN :
                case NORTH :
                default :
                    return SHAPE_NORTH;
                case SOUTH :
                    return SHAPE_SOUTH;
                case EAST :
                    return SHAPE_EAST;
                case WEST :
                    return SHAPE_WEST;
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        //TODO: add item activated method for bottles
        boolean enabled = state.get(ENABLED);
        if(!state.get(HAS_JAR)&&hit.getHitVec().y%1<=0.4375) {
            if (enabled && state.get(HAS_WATER)) {
                Direction dir = state.get(FACING);
                BlockPos backPos = pos.offset(dir.getOpposite());
                BlockState backState = worldIn.getBlockState(backPos);
                BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(backPos.getX() + 0.5,
                        backPos.getY() + 0.5, backPos.getZ() + 0.5), dir, backPos, false);
                ActionResultType blockResult = backState.onBlockActivated(worldIn, player, handIn, rayTraceResult);
                if(blockResult.isSuccessOrConsume())return blockResult;
                ActionResultType itemResult = player.getHeldItem(handIn).getItem().onItemUse(new ItemUseContext(player, handIn, rayTraceResult));
                if(itemResult.isSuccessOrConsume())return itemResult;
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }


        float f = enabled ? 0.6F : 0.5F;
        worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
        this.updateBlock(state, worldIn, pos, true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean hasWater = updateTileFluid(state,pos,worldIn);
        if(hasWater != state.get(HAS_WATER)) worldIn.setBlockState(pos,state.with(HAS_WATER,hasWater));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if(facing==Direction.DOWN)return stateIn.with(HAS_JAR,canConnect(facingState,worldIn,facingPos,facing.getOpposite()));
        if(facing==stateIn.get(FACING).getOpposite()){
            boolean hasWater = updateTileFluid(stateIn,currentPos,worldIn);
            return stateIn.with(EXTENDED,canConnect(facingState,worldIn,facingPos,facing.getOpposite())).with(HAS_WATER,hasWater);
        }
        return stateIn;
    }

    //returns false if no color
    public boolean updateTileFluid(BlockState state, BlockPos pos, IWorld world){
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof FaucetBlockTile){
            return ((FaucetBlockTile) te).updateDisplayedFluid(state);
        }
        return false;
    }

    private boolean canConnect(BlockState downState, IWorld world, BlockPos pos, Direction dir){
        if(downState.getBlock() instanceof JarBlock)return true;
        return world instanceof World && FluidUtil.getFluidHandler((World) world, pos, dir).isPresent();
    }

    //TODO also fix faucet glass connection shading
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updateBlock(state, world, pos, false);
    }

    public void updateBlock(BlockState state, World world, BlockPos pos, boolean toggle) {
        boolean isPowered = world.isBlockPowered(pos);
        if (isPowered != state.get(POWERED) || toggle) {
            world.setBlockState(pos, state.with(POWERED, isPowered).with(ENABLED, toggle ^ state.get(ENABLED)), 2);
        }

        boolean hasWater = updateTileFluid(state,pos,world);
        if(hasWater != state.get(HAS_WATER)) world.setBlockState(pos,state.with(HAS_WATER,hasWater));


        //handles concrete
        if (state.get(ENABLED) ^ toggle ^ isPowered && state.get(HAS_WATER)) {
            BlockPos downPos = pos.down();
            BlockState downState = world.getBlockState(downPos);
            if (downState.getBlock() instanceof ConcretePowderBlock) {
                solidifyConcrete(downPos, state, world);
            }
        }
    }

    public static void solidifyConcrete(BlockPos pos, BlockState state, World world){
        try {
            //field_200294_a ->solidifiedState
            Field f = ObfuscationReflectionHelper.findField(ConcretePowderBlock.class,"field_200294_a");
            f.setAccessible(true);
            world.setBlockState(pos.down(), (BlockState) f.get(state.getBlock()), 2|16);
        } catch (Exception ignored) {}
    }


    public boolean isOpen(BlockState state) {
        return (state.get(BlockStateProperties.POWERED) ^ state.get(BlockStateProperties.ENABLED));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED,FACING, ENABLED, POWERED, HAS_WATER, HAS_JAR, WATERLOGGED, LIGHT_LEVEL);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction dir = context.getFace().getAxis() == Direction.Axis.Y ? Direction.NORTH : context.getFace();

        boolean water = world.getFluidState(pos).getFluid() == Fluids.WATER;
        boolean hasJar = canConnect(world.getBlockState(pos.down()),world,pos.down(),Direction.UP);
        BlockPos backPos = pos.offset(dir.getOpposite());
        boolean jarBehind = canConnect(world.getBlockState(backPos),world,backPos,dir.getOpposite());
        boolean powered = world.isBlockPowered(pos);

        return this.getDefaultState().with(FACING, dir).with(EXTENDED, jarBehind)
                .with(HAS_JAR,hasJar).with(WATERLOGGED,water).with(POWERED,powered);
    }

    //TODO: maybe remove haswater state
    //TODO: add luminance
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        boolean flag = this.isOpen(state);
        if (state.get(HAS_WATER) && !state.get(HAS_JAR)) {
            if(random.nextFloat()>(flag?0:0.06))return;
            float d = 0.125f;
            double x = (pos.getX() + 0.5 + d * (random.nextFloat()-0.5));
            double y = (pos.getY() + 0.25);
            double z = (pos.getZ() + 0.5 + d * (random.nextFloat()-0.5));
            int color = getTileParticleColor(pos,world);
            //get texture color if color is white
            float r = ColorHelper.PackedColor.getRed(color)/255f;
            float g = ColorHelper.PackedColor.getGreen(color)/255f;
            float b = ColorHelper.PackedColor.getBlue(color)/255f;
            world.addParticle(Registry.DRIPPING_LIQUID.get(), x, y, z, r, g, b);

            //world.addParticle(flag?Registry.FALLING_LIQUID.get():Registry.DRIPPING_LIQUID.get(), x, y, z, 0, 1, 0);
        }
    }

    //only client
    public int getTileParticleColor(BlockPos pos, World world){
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof FaucetBlockTile)
            return ((FaucetBlockTile) te).fluidHolder.getParticleColor();
        return 0x423cf7;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FaucetBlockTile();
    }

}

