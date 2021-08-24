package net.mehvahdjukaar.supplementaries.world.explosion;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

;import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a tiny explosion that only destroys surrounding blocks if they have 0
 * hardness (like TNT). Also damages entities standing near it a bit.
 * 
 * @author Tmtravlr (Rebeca Rey), updated by MehVahdJukaar
 * @Date 2015, 2021
 */
public class GunpowderExplosion extends Explosion {

	private final World level;
	private final double x;
	private final double y;
	private final double z;
	private float radius;
	private final List<BlockPos> toBlow = new ArrayList<>();

	public GunpowderExplosion(World world, Entity entity, double x, double y, double z, float size) {
		super(world, entity, x, y, z, size, false, Mode.DESTROY);
		this.level = world;
		this.radius = size;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Create a modified explosion that is meant specifically to set off tnt
	 * next to it.
	 */
	@Override
	public void explode() {
		int x = MathHelper.floor(this.x);
		int y = MathHelper.floor(this.y);
		int z = MathHelper.floor(this.z);

		this.radius *= 2.0F;

		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, new ArrayList<>(), this.radius);

		explodeBlock(x + 1, y, z);
		explodeBlock(x - 1, y, z);
		explodeBlock(x, y + 1, z);
		explodeBlock(x, y - 1, z);
		explodeBlock(x, y, z + 1);
		explodeBlock(x, y, z - 1);

		BlockPos pos = new BlockPos(x,y,z);
		if (this.hasFlammableNeighbours(pos)) {
			this.level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
		}

	}

	private boolean hasFlammableNeighbours(BlockPos pos) {
		for(Direction direction : Direction.values()) {
			if (this.level.getBlockState(pos.relative(direction)).getMaterial().isFlammable()) {
				return true;
			}
		}
		return false;
	}


	public void explodeBlock(int i, int j, int k) {
		BlockPos pos = new BlockPos(i, j, k);
		FluidState fluidstate = this.level.getFluidState(pos);
		if(fluidstate.getType() == Fluids.EMPTY) {
			BlockState state = this.level.getBlockState(pos);
			Block block = state.getBlock();


			if (block.getExplosionResistance(state, this.level, pos, this) == 0) {
				if (!block.isAir(state, level, pos) && block != Blocks.FIRE && block instanceof TNTBlock) {
					this.toBlow.add(pos);
				}
			}
		}
	}

	//needed cause toBlow is private
	@Override
	public void finalizeExplosion(boolean flag) {

		ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
		Collections.shuffle(this.toBlow, this.level.random);

		for(BlockPos blockpos : this.toBlow) {
			BlockState blockstate = this.level.getBlockState(blockpos);

			BlockPos immutable = blockpos.immutable();
			this.level.getProfiler().push("explosion_blocks");
			if (blockstate.canDropFromExplosion(this.level, blockpos, this) && this.level instanceof ServerWorld) {
				TileEntity tileentity = blockstate.hasTileEntity() ? this.level.getBlockEntity(blockpos) : null;
				LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.level)).withRandom(this.level.random)
						.withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockpos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY)
						.withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity).withOptionalParameter(LootParameters.THIS_ENTITY, null);

				builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.radius);

				blockstate.getDrops(builder).forEach((d) -> addBlockDrops(drops, d, immutable));
			}

			blockstate.onBlockExploded(this.level, blockpos, this);
			this.level.getProfiler().pop();

		}

		for(Pair<ItemStack, BlockPos> pair : drops) {
			Block.popResource(this.level, pair.getSecond(), pair.getFirst());
		}
	}



	private void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
		int i = drops.size();
		for(int j = 0; j < i; ++j) {
			Pair<ItemStack, BlockPos> pair = drops.get(j);
			ItemStack itemstack = pair.getFirst();
			if (ItemEntity.areMergable(itemstack, stack)) {
				ItemStack itemstack1 = ItemEntity.merge(itemstack, stack, 16);
				drops.set(j, Pair.of(itemstack1, pair.getSecond()));
				if (stack.isEmpty()) {
					return;
				}
			}
		}
		drops.add(Pair.of(stack, pos));
	}

}
