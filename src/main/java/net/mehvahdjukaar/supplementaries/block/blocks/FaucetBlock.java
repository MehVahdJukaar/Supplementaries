package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.client.Textures;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Random;

public class FaucetBlock extends Block implements  IWaterLoggable{
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
    public static final BooleanProperty HAS_JAR = BlockProperties.HAS_JAR;
    public static final BooleanProperty EXTENDED = BlockStateProperties.ATTACHED; //glass extension
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public FaucetBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HAS_JAR, false).with(FACING, Direction.NORTH)
                .with(ENABLED, false).with(EXTENDED, false).with(POWERED, false).with(HAS_WATER, false).with(WATERLOGGED,false));
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
        float f = state.get(ENABLED) ? 0.6F : 0.5F;
        worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
        this.updateBlock(state, worldIn, pos, true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updateBlock(state, worldIn, pos, false);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn,facing,facingState,worldIn,currentPos,facingPos);
    }

    //TODO: replace this with updatePostPlacement. this will make faucets rotate correctly with turn table
    //TODO!!! also fix faucet glass connection shading
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updateBlock(state, world, pos, false);
    }

    public void updateBlock(BlockState state, World world, BlockPos pos, boolean toggle) {
        BlockPos backpos = pos.offset(state.get(FACING), -1);
        BlockState backblock = world.getBlockState(backpos);
        TileEntity backtile = world.getTileEntity(pos.offset(state.get(FACING), -1));
        // checks backblock
        boolean ispowered = world.getRedstonePowerFromNeighbors(pos) > 0;
        boolean ishoney = backblock.getBlock() instanceof BeehiveBlock && backblock.get(BlockStateProperties.HONEY_LEVEL) > 0;
        boolean backjar = backblock.getBlock() instanceof JarBlock;
        boolean isjarliquid = backjar && backblock.getBlock().getBeaconColorMultiplier(backblock, world, backpos, backpos) != null;
        boolean iswater = (world.getFluidState(backpos).isTagged(FluidTags.WATER)
                || ((backblock.getBlock() instanceof CauldronBlock) && backblock.getComparatorInputOverride(world, backpos) > 0));
        BlockState downstate = world.getBlockState(pos.down());
        boolean hasjar = downstate.getBlock() instanceof JarBlock;
        boolean isTank = false;//backtile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)!=null && !backtile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getFluidInTank(0).isEmpty()).orElse(true);
        boolean haswater = ishoney || iswater || isjarliquid || isTank;


        if (ispowered != state.get(POWERED) || haswater != state.get(HAS_WATER) || hasjar != state.get(HAS_JAR) || toggle||backjar != state.get(EXTENDED)) {
            world.setBlockState(pos,
                    state.with(POWERED, ispowered).with(HAS_WATER, haswater).with(HAS_JAR, hasjar).with(EXTENDED, backjar)
                            .with(ENABLED, toggle ^ state.get(ENABLED)), 2);
        }
        int newcolor = -2;
        //TODO:rewrite this
        if (ishoney)
            newcolor = CommonUtil.JarLiquidType.HONEY.color;
        else if (isjarliquid) {
            TileEntity tileentity = world.getTileEntity(backpos);
            if (tileentity instanceof JarBlockTile) {
                newcolor = ((JarBlockTile) tileentity).color;
            }
        }
        else if (iswater)
            newcolor = -1;
        else if (isTank){
            newcolor = getFluidTankColor(backtile);
        }
        if (newcolor != -2) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof FaucetBlockTile) {
                ((FaucetBlockTile) tileentity).watercolor = newcolor;
            }
        }

        //handles concrete
        if(!hasjar && haswater && (state.get(ENABLED)^toggle^ispowered) && downstate.getBlock() instanceof  ConcretePowderBlock){
            try {
                //field_200294_a ->solidifiedState
                Field f = ObfuscationReflectionHelper.findField(ConcretePowderBlock.class,"field_200294_a");
                f.setAccessible(true);
                world.setBlockState(pos.down(), (BlockState) f.get(downstate.getBlock()), 2|16);
            } catch (Exception ignored) {}
        }


    }

    public static int getFluidTankColor(TileEntity te){
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(iFluidHandler -> iFluidHandler.getFluidInTank(0).getFluid().getAttributes().getColor()).orElse(0xffffff);
    }


    public boolean isOpen(BlockState state) {
        return (state.get(BlockStateProperties.POWERED) ^ state.get(BlockStateProperties.ENABLED));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED,FACING, ENABLED, POWERED, HAS_WATER, HAS_JAR, WATERLOGGED);
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
        boolean flag = world.getFluidState(pos).getFluid() == Fluids.WATER;
        boolean hasjar = world.getBlockState(pos.down()).getBlock() instanceof JarBlock;
        Direction dir = context.getFace();
        if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN) {
            dir = Direction.NORTH;
        }
        boolean jarbehind = world.getBlockState(pos.offset(dir.getOpposite())).getBlock() instanceof JarBlock;
        return this.getDefaultState().with(FACING, dir).with(EXTENDED, jarbehind).with(HAS_JAR,hasjar).with(WATERLOGGED,flag);
    }


    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        super.animateTick(state, world, pos, random);
        if (this.isOpen(state) && state.get(HAS_WATER) && !state.get(HAS_JAR)) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            for (int l = 0; l < 4; ++l) {
                //double d0 = (x + 0.375 + 0.25 * random.nextFloat());
                //double d1 = (y + 0.25 + 0 * random.nextFloat());
                // 0.3125
                //double d2 = (z + 0.375 + 0.25 * random.nextFloat());
               // world.addParticle(ParticleTypes.FALLING_WATER, d0, d1, d2, 0, 0, 0);
                world.addParticle(ParticleTypes.DRIPPING_WATER, x + 0.5, y + 0.25, z + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FaucetBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}

