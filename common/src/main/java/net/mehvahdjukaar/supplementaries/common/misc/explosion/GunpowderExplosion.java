package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
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

    private float radius2;

    public GunpowderExplosion(Level world, Entity entity, double x, double y, double z, float size) {
        super(world, entity, null, null, x, y, z, size, false, BlockInteraction.DESTROY);
        this.radius2 = size;
    }

    /**
     * Create a modified explosion that is meant specifically to set off tnt
     * next to it.
     */
    @Override
    public void explode() {
        int px = Mth.floor(this.x);
        int py = Mth.floor(this.y);
        int pz = Mth.floor(this.z);

        this.radius2 *= 2.0F;

        ForgeHelper.onExplosionDetonate(this.level, this, new ArrayList<>(), this.radius2);

        explodeBlock(px + 1, py, pz);
        explodeBlock(px - 1, py, pz);
        explodeBlock(px, py + 1, pz);
        explodeBlock(px, py - 1, pz);
        explodeBlock(px, py, pz + 1);
        explodeBlock(px, py, pz - 1);

        BlockPos pos = new BlockPos(px, py, pz);
        BlockState newFire = BaseFireBlock.getState(this.level, pos);
        BlockState s = level.getBlockState(pos);
        if (s.canBeReplaced() || s.is(ModRegistry.GUNPOWDER_BLOCK.get())) {
            if (this.hasFlammableNeighbours(pos) || ForgeHelper.isFireSource(this.level.getBlockState(pos.below()), level, pos, Direction.UP)
                    || newFire.getBlock() != Blocks.FIRE) {
                this.level.setBlockAndUpdate(pos, newFire);
            }
        }

    }

    private boolean hasFlammableNeighbours(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = this.level.getBlockState(pos.relative(direction));
            if (state.ignitedByLava()
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
                    this.getToBlow().add(pos);
                }
            }
            //lights up burnable blocks
            if (block instanceof ILightable lightable) {
                lightable.lightUp(null, state, pos, this.level, ILightable.FireSourceType.FLAMING_ARROW);
            } else if (canLight(state)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                ILightable.FireSourceType.FLAMING_ARROW.play(level, pos);
            }
        }
    }

    private static boolean canLight(BlockState state) {
        Block b = state.getBlock();
        if(b instanceof AbstractCandleBlock){
            return !AbstractCandleBlock.isLit(state);
        }
        if(state.hasProperty(BlockStateProperties.LIT) && state.is(ModTags.LIGHTABLE_BY_GUNPOWDER)){
            return !state.getValue(BlockStateProperties.LIT) &&
                    (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED));
        }
        return false;
    }

    @Override
    public ObjectArrayList<BlockPos> getToBlow() {
        return (ObjectArrayList<BlockPos>) super.getToBlow();
    }

    //needed cause toBlow is private
    @Override
    public void finalizeExplosion(boolean spawnFire) {

        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        Util.shuffle(this.getToBlow(), this.level.random);

        for (BlockPos blockpos : this.getToBlow()) {
            BlockState blockstate = this.level.getBlockState(blockpos);

            BlockPos immutable = blockpos.immutable();
            this.level.getProfiler().push("explosion_blocks");
            if (ForgeHelper.canDropFromExplosion(blockstate, this.level, blockpos, this) && this.level instanceof ServerLevel serverLevel) {
                BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                LootParams.Builder builder = (new LootParams.Builder(serverLevel))
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, null);

                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius2);

                blockstate.getDrops(builder).forEach((d) -> addBlockDrops(drops, d, immutable));
            }

            ForgeHelper.onBlockExploded(blockstate, this.level, blockpos, this);
            this.level.getProfiler().pop();

        }

        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }

        if (spawnFire) {
            BlockPos pos = BlockPos.containing(this.x,this.y,this.z);
            if (this.level.getBlockState(pos).isAir() && this.level.getBlockState(pos.below()).isSolidRender(this.level, pos.below())) {
                this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
            }
        }
    }

}
