package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.RopeAttachment;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.PlayerlessContext;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.RopeChandelierBlock;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPistonPlugin;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
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
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
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
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP,true).setValue(DOWN,true).setValue(KNOT,false).setValue(DISTANCE,7).setValue(WATERLOGGED,false)
                .setValue(NORTH, RopeAttachment.NONE).setValue(SOUTH, RopeAttachment.NONE).setValue(EAST, RopeAttachment.NONE).setValue(WEST, RopeAttachment.NONE));
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    public boolean isProtruding(BlockState state){
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(DISTANCE,0).setValue(KNOT,true).setValue(WATERLOGGED,false), VoxelShapes.block());
    }

    //oh boy 32k shapes. 2k by removing water and distance lol
    protected void makeShapes() {
        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.box(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.box(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.box(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.box(6, 9, 6, 16, 13, 10);
        VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);
        VoxelShape northEx = Block.box(6, 9, -6, 10, 13, 10);
        VoxelShape southEx = Block.box(6, 9, 6, 10, 13, 22);
        VoxelShape westEx = Block.box(-6, 9, 6, 10, 13, 10);
        VoxelShape eastEx = Block.box(6, 9, 6, 22, 13, 10);

        for(BlockState state : this.stateDefinition.getPossibleStates()){
            if(state.getValue(WATERLOGGED)||state.getValue(DISTANCE)!=0||!state.getValue(KNOT))continue;
            VoxelShape v = VoxelShapes.or(knot);
            if(state.getValue(DOWN))v = VoxelShapes.or(v,down);
            if(state.getValue(UP))v = VoxelShapes.or(v,up);
            if(state.getValue(NORTH).isBlock())v = VoxelShapes.or(v,north);
            else if(!state.getValue(NORTH).isNone())v = VoxelShapes.or(v,northEx);
            if(state.getValue(SOUTH).isBlock())v = VoxelShapes.or(v,south);
            else if(!state.getValue(SOUTH).isNone())v = VoxelShapes.or(v,southEx);
            if(state.getValue(WEST).isBlock())v = VoxelShapes.or(v,west);
            else if(!state.getValue(WEST).isNone())v = VoxelShapes.or(v,westEx);
            if(state.getValue(EAST).isBlock())v = VoxelShapes.or(v,east);
            else if(!state.getValue(EAST).isNone())v = VoxelShapes.or(v,eastEx);
            v = v.optimize();
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH,SOUTH,EAST,WEST,UP,DOWN,WATERLOGGED,DISTANCE,KNOT);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        return state.getValue(DOWN)&&(state.getValue(UP)||entity.position().y()-pos.getY()<(13/16f));
    }

    //TODO: make solid when player is not colliding
    private static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 13, 16);
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return ((!state.getValue(UP) && (context.isAbove(COLLISION_SHAPE, pos, true)||!state.getValue(DOWN)))
                || !(context.getEntity() instanceof LivingEntity)) ?
                getShape(state,worldIn,pos,context) : VoxelShapes.empty();

    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, worldIn, pos, newState, isMoving);
        if(newState.getBlock()!=this) {
            for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {
                if (state.getValue(FACING_TO_PROPERTY_MAP.get(d)).isKnot()) {
                    this.updateFenceNeighbours(pos, d, worldIn);
                }
            }
        }
    }

    public void updateFenceNeighbours(BlockPos myPos, Direction facingDir, World world){
        BlockPos fencePos = myPos.relative(facingDir);
        BlockState fence = world.getBlockState(fencePos);
        if(CommonUtil.isPost(fence)) {
            for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {
                if (d == facingDir.getOpposite()) continue;
                BlockPos ropePos = fencePos.relative(d);
                BlockState rope = world.getBlockState(ropePos);
                if(rope.getBlock() instanceof RopeBlock){
                    world.setBlock(ropePos,rope.setValue(
                            FACING_TO_PROPERTY_MAP.get(d.getOpposite()),RopeAttachment.KNOT),2|16);
                    return;
                }
            }
        }
    }

    public RopeAttachment getAttachment(BlockPos currentPos, IWorld world, Direction dir){
        BlockPos facingPos = currentPos.relative(dir);
        BlockState facingState = world.getBlockState(facingPos);
        Block b = facingState.getBlock();
        if(b == this)return RopeAttachment.BLOCK;
        else if(CommonUtil.isPost(facingState)){
            if(checkForKnot(facingPos,world,dir))return RopeAttachment.KNOT;
            return RopeAttachment.FENCE;
        }
        return RopeAttachment.NONE;
    }

    //should I become a knot
    private boolean checkForKnot(BlockPos fencePos, IWorld world, Direction myDir) {

        for (Direction d : FACING_TO_PROPERTY_MAP.keySet()) {

            if (d == myDir.getOpposite()) continue;
            BlockState state = world.getBlockState(fencePos.relative(d));
            if (state.getBlock() instanceof RopeBlock) {
                if (state.getValue(FACING_TO_PROPERTY_MAP.get(d.getOpposite())).isKnot()) return false;
                if (d.ordinal() > myDir.ordinal()) return false;
            }
        }
        return true;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (!worldIn.isClientSide()) {
            worldIn.getBlockTicks().scheduleTick(currentPos, this, 1);
        }

        if(facing==Direction.UP) {
            boolean up = isSupportingCeiling(facingPos, worldIn);
            boolean down = up || canConnectDown(currentPos, worldIn);
            stateIn = stateIn.setValue(UP, up).setValue(DOWN, down);
        }
        else if(facing==Direction.DOWN){
            boolean down = canConnectDown(currentPos, worldIn) || stateIn.getValue(UP);
            stateIn = stateIn.setValue(DOWN,down);
            if(!worldIn.isClientSide() && CompatHandler.deco_blocks) RopeChandelierBlock.tryConverting(facingState, worldIn, facingPos);
        }
        else{
            stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(facing),
                    this.getAttachment(currentPos,worldIn,facing));
        }
        return stateIn.setValue(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean hasWater = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        for(Direction dir : FACING_TO_PROPERTY_MAP.keySet()){
            state = state.setValue(FACING_TO_PROPERTY_MAP.get(dir),this.getAttachment(pos,world,dir));
        }
        boolean up = isSupportingCeiling(pos.above(), world);
        boolean down = up || canConnectDown(pos,world);
        state = state.setValue(UP,up).setValue(DOWN,down).setValue(WATERLOGGED,hasWater);
        state = state.setValue(KNOT,hasMiddleKnot(state)).setValue(DISTANCE, getDistance(world, pos));
        return state;
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isClientSide) {
            worldIn.getBlockTicks().scheduleTick(pos, this, 1);
            if(CompatHandler.deco_blocks){
                BlockPos down = pos.below();
                RopeChandelierBlock.tryConverting(worldIn.getBlockState(down), worldIn, down);
            }
        }
    }

    public static boolean hasMiddleKnot(BlockState state){
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        RopeAttachment north = state.getValue(NORTH);
        RopeAttachment east = state.getValue(EAST);
        RopeAttachment south = state.getValue(SOUTH);
        RopeAttachment west = state.getValue(WEST);
        //not inverse
        return !((up&&down&&north.isNone()&&south.isNone()&&east.isNone()&&west.isNone())
                ||(!up&&!down&&!north.isNone()&&!south.isNone()&&east.isNone()&&west.isNone())
                ||(!up&&!down&&north.isNone()&&south.isNone()&&!east.isNone()&&!west.isNone()));

    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return(getDistance(worldIn, pos) < 7);
        //return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    public static boolean isSupportingCeiling(BlockPos pos, IWorldReader world){
        Block b = world.getBlockState(pos).getBlock();
        return canSupportCenter(world, pos, Direction.DOWN)||
                ModTags.isTagged(ModTags.ROPE_SUPPORT_TAG,b);
    }

    public static boolean canConnectDown(BlockPos currentPos, IWorldReader world){
        BlockState state = world.getBlockState(currentPos.below());
        Block b = state.getBlock();
        return (b instanceof RopeBlock || ModTags.isTagged(ModTags.ROPE_HANG_TAG,b) ||
                (state.hasProperty(HorizontalFaceBlock.FACE) && state.getValue(HorizontalFaceBlock.FACE)== AttachFace.CEILING)
                || (b instanceof ChainBlock && state.getValue(BlockStateProperties.AXIS)== Direction.Axis.Y) ||
                (state.hasProperty(BlockStateProperties.HANGING) && state.getValue(BlockStateProperties.HANGING)));
    }

    public static int getDistance(IWorldReader world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutable().move(Direction.UP);
        BlockState blockstate = world.getBlockState(mutable);
        int i = 7;
        if (blockstate.getBlock() instanceof RopeBlock) {
            i = blockstate.getValue(DISTANCE);
        } else if (isSupportingCeiling(mutable, world)) {
            return 0;
        }

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState side = world.getBlockState(mutable.setWithOffset(pos, direction));
            Block b = side.getBlock();
            if (b instanceof RopeBlock) {
                i = Math.min(i, side.getValue(DISTANCE) + 1);
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
        BlockState blockstate = state.setValue(DISTANCE, i);
        if (i == 7) {
            worldIn.destroyBlock(pos, true);
        } else if (state != blockstate) {
            worldIn.setBlock(pos, blockstate, 3);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
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
            return findConnectedBell(world,pos.above(),player,it+1);
        }
        else if(b instanceof BellBlock && it !=0){
            boolean success = ((BellBlock) b).attemptToRing(world, pos, state.getValue(BellBlock.FACING).getClockWise());
            if (success && player != null) {
                player.awardStat(Stats.BELL_RING);
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
            return findConnectedPulley(world,pos.above(),player,it+1,rot);
        }
        else if(b instanceof PulleyBlock && it !=0){
            TileEntity te = world.getBlockEntity(pos);
            if(te instanceof PulleyBlockTile){
                PulleyBlockTile tile = ((PulleyBlockTile) te);
                if(tile.isEmpty() && !player.isShiftKeyDown()){
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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        if(stack.getItem() == this.asItem()){
            if(hit.getDirection().getAxis()==Direction.Axis.Y||state.getValue(DOWN)) {
                if (addRope(pos.below(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType(world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (player == null || !player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
            }
            return ActionResultType.PASS;
        }
        if(stack.isEmpty() && state.getValue(UP)){

            if (ServerConfigs.cached.BELL_CHAIN && findConnectedBell(world, pos, player, 0))
                return ActionResultType.sidedSuccess(world.isClientSide);
            else if(findConnectedPulley(world, pos, player, 0, player.isShiftKeyDown()?Rotation.COUNTERCLOCKWISE_90:Rotation.CLOCKWISE_90)){
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        if(stack.isEmpty() && !player.isShiftKeyDown() && handIn==Hand.MAIN_HAND){
            if(world.getBlockState(pos.below()).getBlock()==this) {
                if (removeRope(pos.below(), world, this)) {
                    world.playSound(player, pos, SoundEvents.LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1, 0.6f);
                    if (player == null || !player.abilities.instabuild) {
                        CommonUtil.swapItem(player, handIn, stack, new ItemStack(this.asItem()));
                    }
                    return ActionResultType.sidedSuccess(world.isClientSide);
                }
            }
        }
        return ActionResultType.PASS;
    }



    public static boolean removeRope(BlockPos pos, World world, Block ropeBlock){
        BlockState state = world.getBlockState(pos);
        if(ropeBlock == state.getBlock()){
            return removeRope(pos.below(), world, ropeBlock);
        }
        else {
            //if (dist == 0) return false;
            BlockPos up = pos.above();
            if(!(world.getBlockState(up).getBlock()==ropeBlock))return false;
            FluidState fromFluid = world.getFluidState(up);
            boolean water = (fromFluid.getType()==Fluids.WATER && fromFluid.isSource());
            world.setBlockAndUpdate(up, water?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());
            tryMove(pos, up, world);
            return true;
        }
    }



    public static boolean addRope(BlockPos pos, World world, @Nullable PlayerEntity player, Hand hand, Block ropeBlock){
        BlockState state = world.getBlockState(pos);
        if(ropeBlock == state.getBlock()){
            return addRope(pos.below(),world,player,hand,ropeBlock);
        }
        else{
            return tryPlaceAndMove(player,hand, (World) world,pos,ropeBlock);
        }
    }


    public static boolean tryPlaceAndMove(@Nullable PlayerEntity player, Hand hand, World world, BlockPos pos, Block ropeBlock) {
        ItemStack stack = new ItemStack(ropeBlock);

        BlockItemUseContext context = new PlayerlessContext(world, player , hand, stack, new BlockRayTraceResult(Vector3d.atCenterOf(pos), Direction.UP, pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.below();
            //try move block down
            if (!(world.getBlockState(downPos).getMaterial().isReplaceable()
                    && tryMove(pos, downPos, world))) return false;
            context = new PlayerlessContext(world, player, hand, stack, new BlockRayTraceResult(Vector3d.atCenterOf(pos), Direction.UP, pos, false));
        }

        BlockState state = ropeBlock.getStateForPlacement(context);
        if (state == null || !CommonUtil.canPlace(context, state)) return false;
        if (state == world.getBlockState(context.getClickedPos()))return false;
        if (world.setBlock(context.getClickedPos(), state, 11)) {
            if(player!=null) {
                BlockState placedState = world.getBlockState(context.getClickedPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.setPlacedBy(world, context.getClickedPos(), placedState, player, stack);
                    if (player instanceof ServerPlayerEntity) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, context.getClickedPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isObsidian(BlockState state){
        return (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.RESPAWN_ANCHOR));
    }

    //TODO: fix order of operations to allow for pulling down lanterns
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, World world) {
        if(toPos.getY()<0||toPos.getY()>255)return false;
        BlockState state = world.getBlockState(fromPos);
        Block block = state.getBlock();
        PushReaction push = state.getPistonPushReaction();

        if ((push==PushReaction.NORMAL||(toPos.getY()<fromPos.getY()&&push==PushReaction.PUSH_ONLY)||ModTags.isTagged(ModTags.ROPE_HANG_TAG,block)) && state.getDestroySpeed(world, fromPos) != -1
                && state.canSurvive(world, toPos) && !block.isAir(state, world, fromPos) && !isObsidian(state)){

            TileEntity tile = world.getBlockEntity(fromPos);
            if (tile != null) {
                //moves everything if quark is not enabled. bad :/ install quark guys
                if(CompatHandler.quark && !QuarkPistonPlugin.canMoveTile(state)){
                    return false;
                }
                else{
                    tile.setRemoved();
                }
            }

            //gets update state for new position

            boolean toFluid = world.getFluidState(toPos).getType()==Fluids.WATER;
            boolean canHoldWater = false;
            if(state.hasProperty(WATERLOGGED)){
                canHoldWater = ModTags.isTagged(ModTags.WATER_HOLDER, state.getBlock());
                if(!canHoldWater) state = state.setValue(WATERLOGGED,toFluid);
            }
            if(state.getBlock() instanceof CauldronBlock && toFluid)state = state.setValue(CauldronBlock.LEVEL,3);


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean leaveWater = (fromFluid.getType()==Fluids.WATER && fromFluid.isSource()) && !canHoldWater;
            world.setBlockAndUpdate(fromPos, leaveWater?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());

            //update existing block block to new position
            BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            world.setBlockAndUpdate(toPos, newState);
            if (tile != null) {
                tile.setPosition(toPos);
                TileEntity target = TileEntity.loadStatic(newState, tile.save(new CompoundNBT()));
                if (target != null) {
                    world.setBlockEntity(toPos, target);
                    target.clearCache();
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborChanged(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        if(entityIn instanceof ArrowEntity && !worldIn.isClientSide){
            worldIn.destroyBlock(pos, true, entityIn);
            worldIn.playSound(null,pos,SoundEvents.LEASH_KNOT_BREAK,SoundCategory.BLOCKS,1,1);
        }
    }

}
