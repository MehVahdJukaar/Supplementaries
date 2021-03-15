package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.RopeAttachment;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.PlayerlessContext;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.plugins.quark.QuarkPistonPlugin;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RopeBlock extends Block implements IWaterLoggable{
    private final Map<BlockState,VoxelShape> SHAPES_MAP = new HashMap<>();

    public static final EnumProperty<RopeAttachment> NORTH = BlockProperties.CONNECTION_NORTH;
    public static final EnumProperty<RopeAttachment> SOUTH = BlockProperties.CONNECTION_SOUTH;
    public static final EnumProperty<RopeAttachment> EAST = BlockProperties.CONNECTION_EAST;
    public static final EnumProperty<RopeAttachment> WEST = BlockProperties.CONNECTION_WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE_0_7;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty KNOT = BlockProperties.KNOT;

    public static final Map<Direction, EnumProperty<RopeAttachment>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
    });
    public RopeBlock(Properties properties) {
        super(properties);
        this.makeShapes();
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(UP,true).with(DOWN,true).with(KNOT,false).with(DISTANCE,7).with(WATERLOGGED,false)
                .with(NORTH, RopeAttachment.NONE).with(SOUTH, RopeAttachment.NONE).with(EAST, RopeAttachment.NONE).with(WEST, RopeAttachment.NONE));
    }

    @Override
    public boolean isReplaceable(BlockState state, Fluid fluid) {
        return false;
    }

    public boolean isProtruding(BlockState state){
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.with(DISTANCE,0).with(KNOT,true).with(WATERLOGGED,false), VoxelShapes.fullCube());
    }

    //oh boy 32k shapes. 2k by removing water and distance lol
    protected void makeShapes() {
        VoxelShape down = Block.makeCuboidShape(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.makeCuboidShape(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.makeCuboidShape(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.makeCuboidShape(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.makeCuboidShape(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.makeCuboidShape(6, 9, 6, 16, 13, 10);
        VoxelShape knot = Block.makeCuboidShape(6, 9, 6, 10, 13, 10);
        VoxelShape northEx = Block.makeCuboidShape(6, 9, -6, 10, 13, 10);
        VoxelShape southEx = Block.makeCuboidShape(6, 9, 6, 10, 13, 22);
        VoxelShape westEx = Block.makeCuboidShape(-6, 9, 6, 10, 13, 10);
        VoxelShape eastEx = Block.makeCuboidShape(6, 9, 6, 22, 13, 10);

        for(BlockState state : this.stateContainer.getValidStates()){
            if(state.get(WATERLOGGED)||state.get(DISTANCE)!=0||!state.get(KNOT))continue;
            VoxelShape v = VoxelShapes.or(knot);
            if(state.get(DOWN))v = VoxelShapes.or(v,down);
            if(state.get(UP))v = VoxelShapes.or(v,up);
            if(state.get(NORTH).isBlock())v = VoxelShapes.or(v,north);
            else if(!state.get(NORTH).isNone())v = VoxelShapes.or(v,northEx);
            if(state.get(SOUTH).isBlock())v = VoxelShapes.or(v,south);
            else if(!state.get(SOUTH).isNone())v = VoxelShapes.or(v,southEx);
            if(state.get(WEST).isBlock())v = VoxelShapes.or(v,west);
            else if(!state.get(WEST).isNone())v = VoxelShapes.or(v,westEx);
            if(state.get(EAST).isBlock())v = VoxelShapes.or(v,east);
            else if(!state.get(EAST).isNone())v = VoxelShapes.or(v,eastEx);
            v = v.simplify();
            boolean flag = true;
            for(VoxelShape existing : this.SHAPES_MAP.values()){
                if(existing.equals(v)){
                    this.SHAPES_MAP.put(state,existing);
                    flag = false;
                    break;
                }
            }
            if(flag) this.SHAPES_MAP.put(state,v);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH,SOUTH,EAST,WEST,UP,DOWN,WATERLOGGED,DISTANCE,KNOT);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return state.get(DOWN)&&entity.getPositionVec().getY()-pos.getY()<(13/16f);
    }

    //TODO: make solid when player is not colliding
    private static final VoxelShape COLLISION_SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 13, 16);
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return ((!state.get(UP) && (context.func_216378_a(COLLISION_SHAPE, pos, true)||!state.get(DOWN)))
                || !(context.getEntity() instanceof LivingEntity)) ?
                getShape(state,worldIn,pos,context) : VoxelShapes.empty();

    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        if(newState.getBlock()!=this) {
            for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {
                if (state.get(FACING_TO_PROPERTY_MAP.get(d)).isKnot()) {
                    this.updateFenceNeighbours(pos, d, worldIn);
                }
            }
        }
    }

    public void updateFenceNeighbours(BlockPos myPos, Direction facingDir, World world){
        BlockPos fencePos = myPos.offset(facingDir);
        BlockState fence = world.getBlockState(fencePos);
        if(CommonUtil.isPost(fence)) {
            for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {
                if (d == facingDir.getOpposite()) continue;
                BlockPos ropePos = fencePos.offset(d);
                BlockState rope = world.getBlockState(ropePos);
                if(rope.getBlock() instanceof RopeBlock){
                    world.setBlockState(ropePos,rope.with(
                            FACING_TO_PROPERTY_MAP.get(d.getOpposite()),RopeAttachment.KNOT),2|16);
                    return;
                }
            }
        }
    }

    //TODO: fix this
    public RopeAttachment getAttachment(BlockPos currentPos, IWorld world, Direction dir){
        BlockPos facingPos = currentPos.offset(dir);
        BlockState facingState = world.getBlockState(facingPos);
        Block b = facingState.getBlock();
        if(b == this)return RopeAttachment.BLOCK;
        else if(CommonUtil.isPost(facingState)){
            if(checkForKnot(facingPos,world,dir))return RopeAttachment.KNOT;
            return RopeAttachment.FENCE;
        }
        //else if(b instanceof WallBlock)return Attachment.WALL;
        return RopeAttachment.NONE;
    }

    //should I become a knot
    private boolean checkForKnot(BlockPos fencePos, IWorld world, Direction myDir) {

        for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {

            if (d == myDir.getOpposite()) continue;
            BlockState state = world.getBlockState(fencePos.offset(d));
            if (state.getBlock() instanceof RopeBlock) {
                if (state.get(FACING_TO_PROPERTY_MAP.get(d.getOpposite())).isKnot()) return false;
                if (d.ordinal() > myDir.ordinal()) return false;
            }
        }
        return true;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if (!worldIn.isRemote()) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }

        if(facing==Direction.UP) {
            boolean up = isSupportingCeiling(facingPos, worldIn);
            boolean down = up || canConnectDown(currentPos, worldIn);
            stateIn = stateIn.with(UP, up).with(DOWN, down);
        }
        else if(facing==Direction.DOWN){
            boolean down = canConnectDown(currentPos, worldIn) || stateIn.get(UP);
            stateIn = stateIn.with(DOWN,down);

        }
        else{
            stateIn = stateIn.with(FACING_TO_PROPERTY_MAP.get(facing),
                    this.getAttachment(currentPos,worldIn,facing));
        }
        return stateIn.with(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        boolean hasWater = context.getWorld().getFluidState(pos).getFluid() == Fluids.WATER;
        BlockState state = this.getDefaultState();
        for(Direction dir : FACING_TO_PROPERTY_MAP.keySet()){
            state = state.with(FACING_TO_PROPERTY_MAP.get(dir),this.getAttachment(pos,world,dir));
        }
        boolean up = isSupportingCeiling(pos.up(), world);
        boolean down = up || canConnectDown(pos,world);
        state = state.with(UP,up).with(DOWN,down).with(WATERLOGGED,hasWater);
        state = state.with(KNOT,hasMiddleKnot(state)).with(DISTANCE, getDistance(world, pos));
        return state;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
        }
    }

    public static boolean hasMiddleKnot(BlockState state){
        boolean up = state.get(UP);
        boolean down = state.get(DOWN);
        RopeAttachment north = state.get(NORTH);
        RopeAttachment east = state.get(EAST);
        RopeAttachment south = state.get(SOUTH);
        RopeAttachment west = state.get(WEST);
        //not inverse
        return !((up&&down&&north.isNone()&&south.isNone()&&east.isNone()&&west.isNone())
                ||(!up&&!down&&!north.isNone()&&!south.isNone()&&east.isNone()&&west.isNone())
                ||(!up&&!down&&north.isNone()&&south.isNone()&&!east.isNone()&&!west.isNone()));

    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return(getDistance(worldIn, pos) < 7);
        //return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    public static boolean isSupportingCeiling(BlockPos pos, IWorldReader world){
        Block b = world.getBlockState(pos).getBlock();
        return hasEnoughSolidSide(world, pos, Direction.DOWN)||
                ModTags.isTagged(ModTags.ROPE_SUPPORT_TAG,b);
    }

    public static boolean canConnectDown(BlockPos currentPos, IWorldReader world){
        BlockState state = world.getBlockState(currentPos.down());
        Block b = state.getBlock();
        return (b instanceof RopeBlock || ModTags.isTagged(ModTags.ROPE_HANG_TAG,b) ||
                (state.hasProperty(HorizontalFaceBlock.FACE) && state.get(HorizontalFaceBlock.FACE)== AttachFace.CEILING)
                || (b instanceof ChainBlock && state.get(BlockStateProperties.AXIS)== Direction.Axis.Y) ||
                (state.hasProperty(BlockStateProperties.HANGING) && state.get(BlockStateProperties.HANGING)));
    }

    public static int getDistance(IWorldReader world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.toMutable().move(Direction.UP);
        BlockState blockstate = world.getBlockState(mutable);
        int i = 7;
        if (blockstate.getBlock() instanceof RopeBlock) {
            i = blockstate.get(DISTANCE);
        } else if (isSupportingCeiling(mutable, world)) {
            return 0;
        }

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState side = world.getBlockState(mutable.setAndMove(pos, direction));
            Block b = side.getBlock();
            if (b instanceof RopeBlock) {
                i = Math.min(i, side.get(DISTANCE) + 1);
                if (i == 1) {
                    break;
                }
            }
            else if(CommonUtil.isPost(side)) i = 0;
        }

        return i;
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int i = getDistance(worldIn, pos);
        BlockState blockstate = state.with(DISTANCE, i);
        if (i == 7) {
            worldIn.destroyBlock(pos, true);
        } else if (state != blockstate) {
            worldIn.setBlockState(pos, blockstate, 3);
        }
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    private static boolean findConnectedBell(World world, BlockPos pos, PlayerEntity player, int it){
        if(it>ServerConfigs.cached.BELL_CHAIN_LENGTH)return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if(b instanceof RopeBlock){
            return findConnectedBell(world,pos.up(),player,it+1);
        }
        else if(b instanceof BellBlock && it !=0){
            boolean success = ((BellBlock) b).ring(world, pos, state.get(BellBlock.HORIZONTAL_FACING).rotateY());
            if (success && player != null) {
                player.addStat(Stats.BELL_RING);
            }
            return true;
        }
        return false;
    }
    private static boolean findConnectedPulley(World world, BlockPos pos, PlayerEntity player, int it, Rotation rot){
        if(it>64)return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if(b instanceof RopeBlock){
            return findConnectedPulley(world,pos.up(),player,it+1,rot);
        }
        else if(b instanceof PulleyBlock && it !=0){
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof PulleyBlockTile){
                PulleyBlockTile tile = ((PulleyBlockTile) te);
                if(tile.isEmpty() && !player.isSneaking()){
                    tile.setDisplayedItem(new ItemStack(Registry.ROPE_ITEM.get()));
                    boolean ret = ((PulleyBlock) b).axisRotate(state, pos, world, rot);
                    tile.getDisplayedItem().shrink(1);
                    return ret;
                }
                else {
                    return ((PulleyBlock) b).axisRotate(state, pos, world, rot);
                }
            }
        }
        return false;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(handIn);
        if(stack.getItem() == this.asItem()){
            if(hit.getFace().getAxis()==Direction.Axis.Y||state.get(DOWN)) {
                if (addRope(pos.down(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType(world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (player == null || !player.abilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                    return ActionResultType.func_233537_a_(world.isRemote);
                }
            }
            return ActionResultType.PASS;
        }
        if(stack.isEmpty() && state.get(UP)){

            if (ServerConfigs.cached.BELL_CHAIN && findConnectedBell(world, pos, player, 0))
                return ActionResultType.func_233537_a_(world.isRemote);
            else if(findConnectedPulley(world, pos, player, 0, player.isSneaking()?Rotation.COUNTERCLOCKWISE_90:Rotation.CLOCKWISE_90)){
                return ActionResultType.func_233537_a_(world.isRemote);
            }
        }
        if(stack.isEmpty() && !player.isSneaking() && handIn==Hand.MAIN_HAND){
            if(world.getBlockState(pos.down()).getBlock()==this) {
                if (removeRope(pos.down(), world, this)) {
                    world.playSound(player, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1, 0.6f);
                    if (player == null || !player.abilities.isCreativeMode) {
                        CommonUtil.swapItem(player, handIn, stack, new ItemStack(this.asItem()));
                    }
                    return ActionResultType.func_233537_a_(world.isRemote);
                }
            }
        }
        return ActionResultType.PASS;
    }



    public static boolean removeRope(BlockPos pos, World world, Block ropeBlock){
        BlockState state = world.getBlockState(pos);
        if(ropeBlock == state.getBlock()){
            return removeRope(pos.down(), world, ropeBlock);
        }
        else {
            //if (dist == 0) return false;
            BlockPos up = pos.up();
            if(!(world.getBlockState(up).getBlock()==ropeBlock))return false;
            FluidState fromFluid = world.getFluidState(up);
            boolean water = (fromFluid.getFluid()==Fluids.WATER && fromFluid.isSource());
            world.setBlockState(up, water?Blocks.WATER.getDefaultState():Blocks.AIR.getDefaultState());
            tryMove(pos, up, world);
            return true;
        }
    }



    public static boolean addRope(BlockPos pos, World world, @Nullable PlayerEntity player, Hand hand, Block ropeBlock){
        BlockState state = world.getBlockState(pos);
        if(ropeBlock == state.getBlock()){
            return addRope(pos.down(),world,player,hand,ropeBlock);
        }
        else{
            return tryPlaceAndMove(player,hand, (World) world,pos,ropeBlock);
        }
    }


    public static boolean tryPlaceAndMove(@Nullable PlayerEntity player, Hand hand, World world, BlockPos pos, Block ropeBlock) {
        ItemStack stack = new ItemStack(ropeBlock);

        BlockItemUseContext context = new PlayerlessContext(world, player , hand, stack, new BlockRayTraceResult(Vector3d.copyCentered(pos), Direction.UP, pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.down();
            //try move block down
            if (!(world.getBlockState(downPos).getMaterial().isReplaceable()
                    && tryMove(pos, downPos, world))) return false;
            context = new PlayerlessContext(world, player, hand, stack, new BlockRayTraceResult(Vector3d.copyCentered(pos), Direction.UP, pos, false));
        }

        BlockState state = ropeBlock.getStateForPlacement(context);
        if (state == null || !CommonUtil.canPlace(context, state)) return false;
        if (state == world.getBlockState(context.getPos()))return false;
        if (world.setBlockState(context.getPos(), state, 11)) {
            if(player!=null) {
                BlockState placedState = world.getBlockState(context.getPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.onBlockPlacedBy(world, context.getPos(), placedState, player, stack);
                    if (player instanceof ServerPlayerEntity) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, context.getPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isObsidian(BlockState state){
        return (state.isIn(Blocks.OBSIDIAN) || state.isIn(Blocks.CRYING_OBSIDIAN) || state.isIn(Blocks.RESPAWN_ANCHOR));
    }

    //TODO: fix order of operations to allow for pulling down lanterns
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, World world) {
        BlockState state = world.getBlockState(fromPos);
        Block block = state.getBlock();
        PushReaction push = state.getPushReaction();
        //TODO: add case for glazed terracotta
        if ((push==PushReaction.NORMAL||ModTags.isTagged(ModTags.ROPE_HANG_TAG,block)) && state.getBlockHardness(world, fromPos) != -1
                && state.isValidPosition(world, toPos) && !block.isAir(state, world, fromPos) && !isObsidian(state)){

            TileEntity tile = world.getTileEntity(fromPos);
            if (tile != null) {
                if(!(ModList.get().isLoaded("quark") && !QuarkPistonPlugin.canMoveTile(state))){
                    return false;
                }
                else{
                    tile.remove();
                }
            }

            //gets update state for new position

            boolean toFluid = world.getFluidState(toPos).getFluid()==Fluids.WATER;
            boolean canHoldWater = false;
            if(state.hasProperty(WATERLOGGED)){
                canHoldWater = ModTags.isTagged(ModTags.WATER_HOLDER, state.getBlock());
                if(!canHoldWater) state = state.with(WATERLOGGED,toFluid);
            }
            if(state.getBlock() instanceof CauldronBlock && toFluid)state = state.with(CauldronBlock.LEVEL,3);


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean water = (fromFluid.getFluid()==Fluids.WATER && fromFluid.isSource());
            if(!canHoldWater) world.setBlockState(fromPos, water?Blocks.WATER.getDefaultState():Blocks.AIR.getDefaultState());
            //update existing block block to new position
            BlockState newState = Block.getValidBlockForPosition(state, world, toPos);
            world.setBlockState(toPos, newState);
            if (tile != null) {
                tile.setPos(toPos);
                TileEntity target = TileEntity.readTileEntity(newState, tile.write(new CompoundNBT()));
                if (target != null) {
                    world.setTileEntity(toPos, target);
                    target.updateContainingBlockInfo();
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborChanged(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);
        if(entityIn instanceof ArrowEntity && !worldIn.isRemote){
            worldIn.destroyBlock(pos, true, entityIn);
            worldIn.playSound(null,pos,SoundEvents.ENTITY_LEASH_KNOT_BREAK,SoundCategory.BLOCKS,1,1);
        }
    }

}
