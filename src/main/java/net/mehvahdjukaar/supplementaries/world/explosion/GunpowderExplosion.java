package net.mehvahdjukaar.supplementaries.world.explosion;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

;

/**
 * Creates a tiny explosion that only destroys surrounding blocks if they have 0
 * hardness (like TNT). Also damages entities standing near it a bit.
 * 
 * @author Tmtravlr (Rebeca Rey), updated by MehVahdJukaar
 * @Date 2015, 2021
 */
public class GunpowderExplosion extends Explosion {
	public GunpowderExplosion(World p_i45752_1_, @Nullable Entity p_i45752_2_, double p_i45752_3_, double p_i45752_5_, double p_i45752_7_, float p_i45752_9_, List<BlockPos> p_i45752_10_) {
		super(p_i45752_1_, p_i45752_2_, p_i45752_3_, p_i45752_5_, p_i45752_7_, p_i45752_9_, p_i45752_10_);
	}

//
//
//	private World myWorldObj;
//	private double explosionX;
//	private double explosionY;
//	private double explosionZ;
//	private float explosionSize;
//
//	public GunpowderExplosion(World world, Entity entity, double x, double y, double z, float size) {
//		super(world, entity, x, y, z, size, false, false);
//		this.myWorldObj = world;
//		this.explosionSize = size;
//		this.explosionX = x;
//		this.explosionY = y;
//		this.explosionZ = z;
//	}
//
//	/**
//	 * Create a modified explosion that is meant specifically to set off tnt
//	 * next to it.
//	 */
//	public void doExplosion() {
//		int x = MathHelper.floor(this.explosionX);
//		int y = MathHelper.floor(this.explosionY);
//		int z = MathHelper.floor(this.explosionZ);
//
//		this.explosionSize *= 2.0F;
//		int minX = MathHelper.floor(this.explosionX - (double) this.explosionSize - 1.0D);
//		int minY = MathHelper.floor(this.explosionY - (double) this.explosionSize - 1.0D);
//		int minZ = MathHelper.floor(this.explosionZ - (double) this.explosionSize - 1.0D);
//		int maxX = MathHelper.floor(this.explosionX + (double) this.explosionSize + 1.0D);
//		int maxY = MathHelper.floor(this.explosionY + (double) this.explosionSize + 1.0D);
//		int maxZ = MathHelper.floor(this.explosionZ + (double) this.explosionSize + 1.0D);
//		List<Entity> list = this.myWorldObj.getEntities(null, new AxisAlignedBB(minX,
//				minY, minZ, maxX, maxY, maxZ));
//		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.myWorldObj, this, list, this.explosionSize);
//		Vector3d vec3 = new Vector3d(this.explosionX, this.explosionY, this.explosionZ);
//
//		for (Entity entity : list) {
//			double distanceToExplosion = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ)
//					/ (double) this.explosionSize;
//
//			if (distanceToExplosion <= 1.0D) {
//				double diffX = entity.getX() - this.explosionX;
//				double diffY = entity.getY() + (double) entity.getEyeHeight() - this.explosionY;
//				double diffZ = entity.getZ() - this.explosionZ;
//				double displacement = (double) MathHelper.sqrt_double(diffX * diffX + diffY * diffY + diffZ * diffZ);
//
//				if (displacement != 0.0D) {
//					diffX /= displacement;
//					diffY /= displacement;
//					diffZ /= displacement;
//					double blockDensity = (double) this.myWorldObj.getBlockDensity(vec3, entity.getEntityBoundingBox());
//					double damageAmount = (1.0D - distanceToExplosion) * blockDensity;
//					entity.attackEntityFrom(DamageSource.setExplosionSource(this), (float) ((int) ((damageAmount
//							* damageAmount + damageAmount)
//							/ 2.0D * 8.0D * (double) this.explosionSize + 1.0D)));
//					double protectionMultiplier = EnchantmentProtection.func_92092_a(entity, damageAmount);
//					entity.motionX += diffX * protectionMultiplier;
//					entity.motionY += diffY * protectionMultiplier;
//					entity.motionZ += diffZ * protectionMultiplier;
//
//					if (entity instanceof EntityPlayer) {
//						this.getPlayerKnockbackMap().put((EntityPlayer) entity,
//								new Vec3(diffX * damageAmount, diffY * damageAmount, diffZ * damageAmount));
//					}
//				}
//			}
//		}
//
//		explodeBlock(x + 1, y, z);
//		explodeBlock(x - 1, y, z);
//		explodeBlock(x, y + 1, z);
//		explodeBlock(x, y - 1, z);
//		explodeBlock(x, y, z + 1);
//		explodeBlock(x, y, z - 1);
//
//		explodeBlock(x + 1, y - 1, z);
//		explodeBlock(x - 1, y - 1, z);
//		explodeBlock(x, y - 1, z + 1);
//		explodeBlock(x, y - 1, z - 1);
//
//		if (PlaceableGunpowder.setFireProperty.getBoolean()
//				&& Blocks.fire.canCatchFire(this.myWorldObj, new BlockPos(x, y - 1, z), EnumFacing.UP)) {
//			this.myWorldObj.setBlockState(new BlockPos(x, y, z), Blocks.fire.getDefaultState());
//		}
//	}
//
//	public void explodeBlock(int i, int j, int k) {
//		BlockPos pos = new BlockPos(i, j, k);
//		BlockState blockState = this.myWorldObj.getBlockState(pos);
//		Block block = blockState.getBlock();
//
//		if (block.getExplosionResistance(null) == 0 && block != Blocks.fire) {
//			if (block.getMaterial() != Material.air) {
//				if (block.canDropFromExplosion(this)) {
//					block.dropBlockAsItemWithChance(this.myWorldObj, pos, blockState, 1.0F / this.explosionSize, 0);
//				}
//
//				block.onBlockExploded(this.myWorldObj, pos, this);
//			}
//		}
//	}

}
