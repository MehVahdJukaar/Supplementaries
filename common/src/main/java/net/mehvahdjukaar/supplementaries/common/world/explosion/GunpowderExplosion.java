package net.mehvahdjukaar.supplementaries.common.world.explosion;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.ForgeHelper;
import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * Creates a tiny explosion that only destroys surrounding blocks if they have 0
 * hardness (like TNT). Also damages entities standing near it a bit.
 *
 * @author Tmtravlr (Rebeca Rey), updated by MehVahdJukaar
 * @Date 2015, 2021
 */
public class GunpowderExplosion extends Explosion {

    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    private float radius;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();

    public GunpowderExplosion(Level world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, null, null, x, y, z, size, false, BlockInteraction.DESTROY);
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
        int x = Mth.floor(this.x);
        int y = Mth.floor(this.y);
        int z = Mth.floor(this.z);

        this.radius *= 2.0F;

        ForgeHelper.onExplosionDetonate(this.level, this, new ArrayList<>(), this.radius);

        explodeBlock(x + 1, y, z);
        explodeBlock(x - 1, y, z);
        explodeBlock(x, y + 1, z);
        explodeBlock(x, y - 1, z);
        explodeBlock(x, y, z + 1);
        explodeBlock(x, y, z - 1);

        BlockPos pos = new BlockPos(x, y, z);
        BlockState newFire = BaseFireBlock.getState(this.level, pos);
        BlockState s = level.getBlockState(pos);
        if (s.getMaterial().isReplaceable() || s.is(ModRegistry.GUNPOWDER_BLOCK.get())) {
            if (this.hasFlammableNeighbours(pos) || ForgeHelper.isFireSource(this.level.getBlockState(pos.below()), level, pos, Direction.UP)
                    || newFire.getBlock() != Blocks.FIRE) {
                this.level.setBlockAndUpdate(pos, newFire);
            }
        }

    }

    private boolean hasFlammableNeighbours(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = this.level.getBlockState(pos.relative(direction));
            if (state.getMaterial().isFlammable()
                    || (state.getBlock() == ModRegistry.BELLOWS.get() && state.getValue(BellowsBlock.POWER) != 0 &&
                    state.getValue(BellowsBlock.FACING) == direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }


    private void explodeBlock(int i, int j, int k) {
        BlockPos pos = new BlockPos(i, j, k);
        FluidState fluidstate = this.level.getFluidState(pos);
        if (fluidstate.getType() == Fluids.EMPTY) {
            BlockState state = this.level.getBlockState(pos);
            Block block = state.getBlock();


            if (ForgeHelper.getExplosionResistance(state, this.level, pos, this) == 0) {
                if (block instanceof TntBlock) {
                    this.toBlow.add(pos);
                }
            }
            //lights up burnable blocks
            if (block instanceof ILightable lightable) {
                lightable.lightUp(null, state, pos, this.level, ILightable.FireSound.FLAMING_ARROW);
            } else if ((state.is(BlockTags.CAMPFIRES) && CampfireBlock.canLight(state)) ||
                    (state.getBlock() instanceof AbstractCandleBlock && !AbstractCandleBlock.isLit(state)) ||
                    (CompatHandler.deco_blocks && DecoBlocksCompat.canLightBrazier(state))) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                ILightable.FireSound.FLAMING_ARROW.play(level, pos);
            }
        }
    }

    //needed cause toBlow is private
    @Override
    public void finalizeExplosion(boolean spawnFire) {

        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        Util.shuffle(this.toBlow, this.level.random);

        for (BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);

            BlockPos immutable = blockpos.immutable();
            this.level.getProfiler().push("explosion_blocks");
            if (ForgeHelper.canDropFromExplosion(blockstate, this.level, blockpos, this) && this.level instanceof ServerLevel) {
                BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                LootContext.Builder builder = (new LootContext.Builder((ServerLevel) this.level)).withRandom(this.level.random)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, null);

                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);

                blockstate.getDrops(builder).forEach((d) -> addBlockDrops(drops, d, immutable));
            }

            ForgeHelper.onBlockExploded(blockstate, this.level, blockpos, this);
            this.level.getProfiler().pop();

        }

        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }

        if (spawnFire) {
            BlockPos pos = new BlockPos(this.x,this.y,this.z);
            if (this.level.getBlockState(pos).isAir() && this.level.getBlockState(pos.below()).isSolidRender(this.level, pos.below())) {
                this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
            }
        }
    }

    private void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
        int i = drops.size();
        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = drops.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, stack)) {
                ItemStack merge = ItemEntity.merge(itemstack, stack, 16);
                drops.set(j, Pair.of(merge, pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        drops.add(Pair.of(stack, pos));
    }

}
