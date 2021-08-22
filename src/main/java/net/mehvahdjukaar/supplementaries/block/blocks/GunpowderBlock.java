package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

/**
 * The main gunpowder block. Very similar to the {@link net.minecraft.block.RedstoneWireBlock}
 * block.
 * 
 * @author Tmtravlr (Rebeca Rey), updated by MehVahdJukaar
 * @Date December 2015, 2021
 */
public class GunpowderBlock extends LightUpBlock {

	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty BURNING = BlockProperties.BURNING;


	public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
	private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
	private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
	private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
	private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
	private final BlockState crossState;


	public static final int DELAY = 1;

	/*
	static {
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.GUNPOWDER, new BehaviorDefaultDispenseItem() {

			public ItemStack dispenseStack(IBlockSource blockSource, ItemStack stack) {
				EnumFacing enumfacing = BlockDispenser.getFacing(blockSource.getBlockState());
				IPosition iposition = BlockDispenser.getDispensePosition(blockSource);
				int x = MathHelper.floor_double(blockSource.getX()) + enumfacing.getFrontOffsetX();
				int y = MathHelper.floor_double(blockSource.getY()) + enumfacing.getFrontOffsetY();
				int z = MathHelper.floor_double(blockSource.getZ()) + enumfacing.getFrontOffsetZ();
				BlockPos pos = new BlockPos(x, y, z);
				if (GunpowderBlock.instance.canPlaceBlockAt(blockSource.getWorld(), pos)
						&& blockSource.getWorld().getBlockState(pos).getBlock()
								.isReplaceable(blockSource.getWorld(), pos)) {
					blockSource.getWorld().setBlockState(pos, GunpowderBlock.instance.getDefaultState());
					stack.splitStack(1);
				}
				return stack;
			}
		});
	}
	*/

	public GunpowderBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE)
				.setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE)
				.setValue(WEST, RedstoneSide.NONE).setValue(BURNING, 0));
		this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE)
				.setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE)
				.setValue(WEST, RedstoneSide.SIDE).setValue(BURNING, 0);

		for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
			if (blockstate.getValue(BURNING) == 0) {
				this.SHAPES_CACHE.put(blockstate, this.calculateVoxelShape(blockstate));
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, BURNING);
	}

	private VoxelShape calculateVoxelShape(BlockState state) {
		VoxelShape voxelshape = SHAPE_DOT;

		for(Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneside == RedstoneSide.SIDE) {
				voxelshape = VoxelShapes.or(voxelshape, SHAPES_FLOOR.get(direction));
			} else if (redstoneside == RedstoneSide.UP) {
				voxelshape = VoxelShapes.or(voxelshape, SHAPES_UP.get(direction));
			}
		}
		return voxelshape;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return this.SHAPES_CACHE.get(state.setValue(BURNING, 0));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getConnectionState(context.getLevel(), this.crossState, context.getClickedPos());
	}

	//-----connection logic------

	private BlockState getConnectionState(IBlockReader world, BlockState state, BlockPos pos) {
		boolean flag = isDot(state);
		state = this.getMissingConnections(world, this.defaultBlockState().setValue(BURNING, state.getValue(BURNING)), pos);
		if (!flag || !isDot(state)) {
			boolean flag1 = state.getValue(NORTH).isConnected();
			boolean flag2 = state.getValue(SOUTH).isConnected();
			boolean flag3 = state.getValue(EAST).isConnected();
			boolean flag4 = state.getValue(WEST).isConnected();
			boolean flag5 = !flag1 && !flag2;
			boolean flag6 = !flag3 && !flag4;
			if (!flag4 && flag5) {
				state = state.setValue(WEST, RedstoneSide.SIDE);
			}

			if (!flag3 && flag5) {
				state = state.setValue(EAST, RedstoneSide.SIDE);
			}

			if (!flag1 && flag6) {
				state = state.setValue(NORTH, RedstoneSide.SIDE);
			}

			if (!flag2 && flag6) {
				state = state.setValue(SOUTH, RedstoneSide.SIDE);
			}
		}
		return state;
	}

	private BlockState getMissingConnections(IBlockReader world, BlockState state, BlockPos pos) {
		boolean flag = !world.getBlockState(pos.above()).isRedstoneConductor(world, pos);

		for(Direction direction : Direction.Plane.HORIZONTAL) {
			if (!state.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
				RedstoneSide redstoneside = this.getConnectingSide(world, pos, direction, flag);
				state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
			}
		}
		return state;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, IWorld world, BlockPos pos, BlockPos otherPos) {
		if (direction == Direction.DOWN) {
			return state;
		} else if (direction == Direction.UP) {
			return this.getConnectionState(world, state, pos);
		} else {
			RedstoneSide redstoneside = this.getConnectingSide(world, pos, direction);
			return redstoneside.isConnected() == state.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && !isCross(state) ?
					state.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside) :
					this.getConnectionState(world, this.crossState.setValue(BURNING, state.getValue(BURNING)).setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside), pos);
		}
	}

	private static boolean isCross(BlockState state) {
		return state.getValue(NORTH).isConnected() && state.getValue(SOUTH).isConnected() && state.getValue(EAST).isConnected() && state.getValue(WEST).isConnected();
	}

	private static boolean isDot(BlockState state) {
		return !state.getValue(NORTH).isConnected() && !state.getValue(SOUTH).isConnected() && !state.getValue(EAST).isConnected() && !state.getValue(WEST).isConnected();
	}

	//used to connect diagonally
	@Override
	public void updateIndirectNeighbourShapes(BlockState state, IWorld world, BlockPos pos, int var1, int var2) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneside != RedstoneSide.NONE && !world.getBlockState(mutable.setWithOffset(pos, direction)).is(this)) {
				mutable.move(Direction.DOWN);
				BlockState blockstate = world.getBlockState(mutable);
				if (!blockstate.is(Blocks.OBSERVER)) {
					BlockPos blockpos = mutable.relative(direction.getOpposite());
					BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), world.getBlockState(blockpos), world, mutable, blockpos);
					updateOrDestroy(blockstate, blockstate1, world, mutable, var1, var2);
				}

				mutable.setWithOffset(pos, direction).move(Direction.UP);
				BlockState blockstate3 = world.getBlockState(mutable);
				if (!blockstate3.is(Blocks.OBSERVER)) {
					BlockPos blockpos1 = mutable.relative(direction.getOpposite());
					BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(), world.getBlockState(blockpos1), world, mutable, blockpos1);
					updateOrDestroy(blockstate3, blockstate2, world, mutable, var1, var2);
				}
			}
		}

	}

	//gets connection to blocks diagonally above
	private RedstoneSide getConnectingSide(IBlockReader world, BlockPos pos, Direction dir) {
		return this.getConnectingSide(world, pos, dir, !world.getBlockState(pos.above()).isRedstoneConductor(world, pos));
	}

	private RedstoneSide getConnectingSide(IBlockReader world, BlockPos pos, Direction dir, boolean canClimbUp) {
		BlockPos blockpos = pos.relative(dir);
		BlockState blockstate = world.getBlockState(blockpos);
		if (canClimbUp) {
			boolean flag = this.canSurviveOn(world, blockpos, blockstate);
			if (flag && canConnectTo(world.getBlockState(blockpos.above()), world, blockpos.above(), null) ) {
				if (blockstate.isFaceSturdy(world, blockpos, dir.getOpposite())) {
					return RedstoneSide.UP;
				}
				return RedstoneSide.SIDE;
			}
		}
		return !canConnectTo(blockstate, world, blockpos, dir) && (blockstate.isRedstoneConductor(world, blockpos) || !canConnectTo(world.getBlockState(blockpos.below()), world, blockpos.below(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = world.getBlockState(blockpos);
		return this.canSurviveOn(world, blockpos, blockstate);
	}

	private boolean canSurviveOn(IBlockReader world, BlockPos pos, BlockState state) {
		return state.isFaceSturdy(world, pos, Direction.UP) || state.is(Blocks.HOPPER);
	}

	//TODO: add more cases
	protected boolean canConnectTo(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction dir) {
		if (state.is(this.asBlock())) {
			return true;
		} else {
			return state.getBlock() instanceof TNTBlock;
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		switch(rotation) {
			case CLOCKWISE_180:
				return state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
			case CLOCKWISE_90:
				return state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
			default:
				return state;
		}
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		switch(mirror) {
			case LEFT_RIGHT:
				return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
			case FRONT_BACK:
				return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
			default:
				return super.mirror(state, mirror);
		}
	}



	//-----redstone------

	private void updatePowerStrength(World world, BlockPos pos, BlockState state) {
	}


	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
		if (!oldState.is(state.getBlock()) && !world.isClientSide) {
			this.updatePowerStrength(world, pos, state);

			for(Direction direction : Direction.Plane.VERTICAL) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}

			this.updateNeighborsOfNeighboringWires(world, pos);
		}
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && !state.is(newState.getBlock())) {
			super.onRemove(state, world, pos, newState, isMoving);
			if (!world.isClientSide) {
				for(Direction direction : Direction.values()) {
					world.updateNeighborsAt(pos.relative(direction), this);
				}
				this.updatePowerStrength(world, pos, state);
				this.updateNeighborsOfNeighboringWires(world, pos);
			}
		}
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which
	 * neighbor changed (coordinates passed are their own) Args: x, y, z,
	 * neighbor Block
	 */
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moving) {
		if (!world.isClientSide) {
			if (state.canSurvive(world, pos)) {
				if (checkNeighboursForFire(world, pos)) {
					this.ignite(world, pos);
				}
			} else {
				dropResources(state, world, pos);
				world.removeBlock(pos, false);
			}
		}
	}

	private void updateNeighborsOfNeighboringWires(World world, BlockPos pos) {
		for(Direction direction : Direction.Plane.HORIZONTAL) {
			this.checkCornerChangeAt(world, pos.relative(direction));
		}

		for(Direction direction1 : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction1);
			if (world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				this.checkCornerChangeAt(world, blockpos.above());
			} else {
				this.checkCornerChangeAt(world, blockpos.below());
			}
		}
	}

	private void checkCornerChangeAt(World world, BlockPos pos) {
		if (world.getBlockState(pos).is(this)) {
			world.updateNeighborsAt(pos, this);

			for(Direction direction : Direction.values()) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (player.abilities.mayBuild) {
			if (isCross(state) || isDot(state)) {
				BlockState blockstate = isCross(state) ? this.defaultBlockState() : this.crossState;
				blockstate = blockstate.setValue(BURNING, state.getValue(BURNING));
				blockstate = this.getConnectionState(world, blockstate, pos);
				if (blockstate != state) {
					world.setBlock(pos, blockstate, 3);
					this.updatesOnShapeChange(world, pos, state, blockstate);
					return ActionResultType.SUCCESS;
				}
			}

		}
		return ActionResultType.PASS;
	}

	private void updatesOnShapeChange(World world, BlockPos pos, BlockState state, BlockState newState) {
		for(Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction);
			if (state.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != newState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				world.updateNeighborsAtExceptFromFacing(blockpos, newState.getBlock(), direction.getOpposite());
			}
		}

	}





	//-----explosion-stuff------


	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int burning = state.getValue(BURNING);

		if (burning > 0) {
			if (burning == 8) {
				world.removeBlock(pos, false);
				explode(world, pos);
			} else {
				if (burning >= 4) {
					this.igniteNeighbours(world, pos);
				}
				world.setBlockAndUpdate(pos, state.setValue(BURNING, burning + 1));
				world.getBlockTicks().scheduleTick(pos, this, DELAY);
			}
		}
	}

	public static void explode(World world, BlockPos pos) {
		/*
		GunpowderExplosion explosion = new GunpowderExplosion(world, null, pos.getX(), pos.getY(), pos.getZ(), 0.5f);
		if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
			return;
		explosion.doExplosion();
		*/
	}


	private boolean checkNeighboursForFire(World world, BlockPos pos) {
		return world.getBlockState(pos.above()).getBlock() == Blocks.FIRE
				|| world.getBlockState(pos.below()).getBlock() == Blocks.FIRE
				|| world.getBlockState(pos.east()).getBlock() == Blocks.FIRE
				|| world.getBlockState(pos.west()).getBlock() == Blocks.FIRE
				|| world.getBlockState(pos.north()).getBlock() == Blocks.FIRE
				|| world.getBlockState(pos.south()).getBlock() == Blocks.FIRE;
	}
	//TODO: drop nothing if burning
//
//
//	/**
//	 * Triggered whenever an entity collides with this block (enters into the
//	 * block). Args: world, x, y, z, entity
//	 */
//	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
//		if (entity instanceof EntityArrow && !world.isRemote) {
//			EntityArrow entityarrow = (EntityArrow) entity;
//
//			if (entityarrow.isBurning()) {
//				this.ignite(world, pos);
//			}
//		}
//	}
//
//	/**
//	 * Called upon block activation (right click on the block.)
//	 */
//	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
//			float hitX, float hitY, float hitZ) {
//		if (player.getCurrentEquippedItem() != null
//				&& player.getCurrentEquippedItem().getItem() == Items.flint_and_steel) {
//			this.ignite(world, pos);
//			player.getCurrentEquippedItem().damageItem(1, player);
//			return true;
//		} else {
//			return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
//		}
//	}
//
//	/**
//	 * Called upon the block being destroyed by an explosion
//	 */
//	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
//		if (!world.isRemote) {
//			if (this.canPlaceBlockAt(world, pos)) {
//				world.setBlockState(pos, this.getDefaultState());
//				this.ignite(world, pos);
//			}
//		}
//	}

	public void ignite(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == this) {
			Integer power = (Integer) state.getValue(BURNING);

			if (power == 0) {
				//world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
				//		(double) pos.getZ() + 0.5D, PlaceableGunpowder.MODID + ":gunpowder.ignite", 1.0f,
				//		1.9f + world.random.nextFloat() * 0.1f);
				world.setBlockAndUpdate(pos, state.setValue(BURNING, 1));
				world.getBlockTicks().scheduleTick(pos, this, DELAY);
			}
		}
	}

	public void igniteNeighbours(World world, BlockPos pos) {
		/*
		ignite(world, pos.above());
		ignite(world, pos.below());
		ignite(world, pos.east());
		ignite(world, pos.west());
		ignite(world, pos.north());
		ignite(world, pos.south());

		if (world.getBlockState(pos.east()).getBlock().isNormalCube()) {
			ignite(world, pos.east().up());
		} else {
			ignite(world, pos.east().down());
		}

		if (world.getBlockState(pos.west()).getBlock().isNormalCube()) {
			ignite(world, pos.west().up());
		} else {
			ignite(world, pos.west().down());
		}

		if (world.getBlockState(pos.north()).getBlock().isNormalCube()) {
			ignite(world, pos.north().up());
		} else {
			ignite(world, pos.north().down());
		}

		if (world.getBlockState(pos.south()).getBlock().isNormalCube()) {
			ignite(world, pos.south().up());
		} else {
			ignite(world, pos.south().down());
		}
		*/
	}
	//----- light up block ------


	@Override
	public boolean isLit(BlockState state) {
		return state.getValue(BURNING) != 0;
	}

	@Override
	public BlockState toggleListState(BlockState state, boolean lit) {
		return state.setValue(BURNING, lit ? 1 : 0);
	}

	/**
	 * Gets an item for the block being called on. Args: world, x, y, z
	 */
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(Items.GUNPOWDER);
	}


	//client

	/**
	 * A randomly called display update to be able to add particles or other
	 * items for display
	 */
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		int i = state.getValue(BURNING);
		if (i != 0) {
			for(Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
				switch(redstoneside) {
					case UP:
						this.spawnParticlesAlongLine(world, random, pos, i, direction, Direction.UP, -0.5F, 0.5F);
					case SIDE:
						this.spawnParticlesAlongLine(world, random, pos, i, Direction.DOWN, direction, 0.0F, 0.5F);
						break;
					case NONE:
					default:
						this.spawnParticlesAlongLine(world, random, pos, i, Direction.DOWN, direction, 0.0F, 0.3F);
				}
			}

		}
	}

	private void spawnParticlesAlongLine(World world, Random rand, BlockPos pos, int burning, Direction dir1, Direction dir2, float from, float to) {
		float f = to - from;
		if (!(rand.nextFloat() >= 0.2F * f)) {
			float f2 = from + f * rand.nextFloat();
			double x = pos.getX() + 0.5D + (double)(0.4375F * (float) dir1.getStepX()) + (double)(f2 * (float)dir2.getStepX());
			double y = pos.getY() + 0.5D + (double)(0.4375F * (float) dir1.getStepY()) + (double)(f2 * (float)dir2.getStepY());
			double z = pos.getZ() + 0.5D + (double)(0.4375F * (float) dir1.getStepZ()) + (double)(f2 * (float)dir2.getStepZ());

			float velY = (burning / 15.0F) * 0.03F;
			float velX = rand.nextFloat() * 0.02f - 0.01f;
			float velZ = rand.nextFloat() * 0.02f - 0.01f;

			world.addParticle(ParticleTypes.FLAME, x, y, z, velX, velY, velZ);
			world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, velX, velY, velZ);
		}
	}


}
