package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.TrinketsCompat;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
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

    private static final Holder<SoundEvent> EMPTY_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY);

    //same as Level.explode but with custom subclass. client one just gets the normal one
    public static void explode(ServerLevel world, BlockPos pos) {
        //TODO: maybe this could be done just with a custom block interaction
        Vec3 center = Vec3.atCenterOf(pos);
        GunpowderExplosion explosion = new GunpowderExplosion(world, center.x, center.y, center.z);
        if (ForgeHelper.fireOnExplosionStart(world, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(false);

        //same as server level explode
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for(ServerPlayer serverPlayer : world.players()) {
            if (serverPlayer.distanceToSqr(center) < 4096.0) {
                serverPlayer.connection
                        .send(
                                new ClientboundExplodePacket(
                                        center.x, center.y, center.z,
                                        explosion.radius(),
                                        explosion.getToBlow(),
                                        explosion.getHitPlayers().get(serverPlayer),
                                        explosion.getBlockInteraction(),
                                        explosion.getSmallExplosionParticles(),
                                        explosion.getLargeExplosionParticles(),
                                        explosion.getExplosionSound()
                                )
                        );
            }
        }
    }

    public GunpowderExplosion(Level world, double x, double y, double z) {
        super(world, null, null, null,
                x, y, z, 0.5f, false,
                BlockInteraction.DESTROY, ParticleTypes.ASH, ParticleTypes.ASH, EMPTY_SOUND);
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
        //is this needed?
        ForgeHelper.fireOnExplosionDetonate(this.level, this, new ArrayList<>(), this.radius * 2f);

        explodeSingleBlock(px + 1, py, pz);
        explodeSingleBlock(px - 1, py, pz);
        explodeSingleBlock(px, py + 1, pz);
        explodeSingleBlock(px, py - 1, pz);
        explodeSingleBlock(px, py, pz + 1);
        explodeSingleBlock(px, py, pz - 1);
        //explode ones above for gunpowder climb
        explodeSingleBlock(px, py + 1, pz + 1);
        explodeSingleBlock(px, py + 1, pz - 1);
        explodeSingleBlock(px + 1, py + 1, pz);
        explodeSingleBlock(px - 1, py + 1, pz);


        BlockPos myPos = new BlockPos(px, py, pz);
        BlockState newFire = BaseFireBlock.getState(this.level, myPos);
        BlockState s = level.getBlockState(myPos);
        if (s.canBeReplaced() && level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) { //kills my own gunpowder block
            if (this.hasFlammableNeighbours(myPos)
                    || newFire.getBlock() != Blocks.FIRE) {
                 if(s.is(ModRegistry.GUNPOWDER_BLOCK.get()) && s.getValue(GunpowderBlock.TYPE).isHidden())return;

                    this.level.setBlockAndUpdate(myPos, newFire);
                //s.onCaughtFire(arg, arg2, Direction.UP, null);
            }
        }
    }

    private boolean hasFlammableNeighbours(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = this.level.getBlockState(pos.relative(direction));
            if ((PlatHelper.isFireSource(state, level, pos, direction.getOpposite()) ||
                    PlatHelper.isFlammable(state, level, pos, direction.getOpposite()))) {
                return true;
            }
        }
        return false;
    }


    private void explodeSingleBlock(int i, int j, int k) {
        BlockPos pos = new BlockPos(i, j, k);
        FluidState fluidstate = this.level.getFluidState(pos);
        if (fluidstate.getType() == Fluids.EMPTY || fluidstate.getType() == ModFluids.LUMISENE_FLUID.get()) {
            BlockState state = this.level.getBlockState(pos);
            Block block = state.getBlock();


            if (ForgeHelper.getExplosionResistance(state, this.level, pos, this) == 0) {
                if (block instanceof TntBlock) {
                    this.getToBlow().add(pos);
                } else if (block == CompatObjects.NUKE_BLOCK.get()) {
                    igniteTntHack(this.level, pos, state);
                }
            }
            //lights up burnable blocks
            if (block instanceof ILightable iLightable) {
                iLightable.tryLightUp(null, state, pos, this.level, ILightable.FireSoundType.FLAMING_ARROW);
            } else if (canLight(state)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                ILightable.FireSoundType.FLAMING_ARROW.play(level, pos);
            }
        }
    }

    private static boolean canLight(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof AbstractCandleBlock) {
            return !AbstractCandleBlock.isLit(state);
        }
        if (state.hasProperty(BlockStateProperties.LIT) && state.is(ModTags.LIGHTABLE_BY_GUNPOWDER)) {
            return !state.getValue(BlockStateProperties.LIT) &&
                    (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED));
        }
        return false;
    }

    //TODO: remove
    // specifically for alex caves nukes basically
    public static void igniteTntHack(Level level, BlockPos blockpos, BlockState tnt) {
        Arrow dummyArrow = new Arrow(level, blockpos.getX() + 0.5, blockpos.getY() + 0.5,
                blockpos.getZ() + 0.5, Items.ARROW.getDefaultInstance(), null);
        dummyArrow.setRemainingFireTicks(20);
        BlockState old = level.getBlockState(blockpos);
        //this will remove block above. too bad we are about to explode it anyways
        tnt.onProjectileHit(level, tnt,
                new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, blockpos, true),
                dummyArrow);
        //restore old block
        level.setBlockAndUpdate(blockpos, old);
    }

}
