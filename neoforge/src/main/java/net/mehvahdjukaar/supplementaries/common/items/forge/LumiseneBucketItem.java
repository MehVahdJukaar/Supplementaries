package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.HatStandArmorLayer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FirePitBlock;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.common.items.BubbleBlowerItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class LumiseneBucketItem extends BucketItem {
    private final int capacity;

    public LumiseneBucketItem(Supplier<? extends FiniteFluid> supplier, Properties builder, int capacity) {
        super(supplier, builder);
        this.capacity = capacity;
    }

    // we override this JUSt so we can place on water

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);

        // changed here
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onBucketUse(player, level, itemstack, blockhitresult);
        if (ret != null) {
            return ret;
        } else if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos blockpos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos above = blockpos.relative(direction);
            //manual check since we manually raytrace


            if (!level.mayInteract(player, blockpos) || !player.mayUseItemAt(above, direction, itemstack)) {
                return InteractionResultHolder.fail(itemstack);
            } else {
                BlockState blockstate = level.getBlockState(blockpos);
                BlockPos blockpos2 = this.canBlockContainFluid(level, blockpos, blockstate) ? blockpos : above;
                if (this.emptyContents(player, level, blockpos2, blockhitresult, itemstack)) {
                    this.checkExtraContent(player, level, itemstack, blockpos2);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos2, itemstack);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemstack, player), level.isClientSide());
                } else {
                    return InteractionResultHolder.fail(itemstack);
                }
            }
        }
    }


    // we have to override this because we need to place different layers or something
//TODO: floats on water here
    @Override
    public boolean emptyContents(@Nullable Player arg, Level level, BlockPos pos, @Nullable BlockHitResult arg4, @Nullable ItemStack container) {
        Fluid myFluid = this.getFluid();
        BlockState blockstate = level.getBlockState(pos);
        Block block = blockstate.getBlock();
        boolean flag = blockstate.canBeReplaced(myFluid);
        boolean flag1 = blockstate.isAir() || flag || block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(level, pos, blockstate, myFluid);
        Optional<FluidStack> containedFluidStack = Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);
        if (!flag1) {
            return arg4 != null && this.emptyContents(arg, level, arg4.getBlockPos().relative(arg4.getDirection()), null, container);
        } else if (containedFluidStack.isPresent() && myFluid.getFluidType().isVaporizedOnPlacement(level, pos, containedFluidStack.get())) {
            myFluid.getFluidType().onVaporize(arg, level, pos, containedFluidStack.get());
            return true;
        } else if (level.dimensionType().ultraWarm() && myFluid.is(FluidTags.WATER)) {
            //TODO: instant catch fire here
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            level.playSound(arg, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0, 0.0, 0.0);
            }

            return true;
        } else if (block instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(level, pos, blockstate, myFluid)) {
            lc.placeLiquid(level, pos, blockstate, myFluid.defaultFluidState()
                    .setValue(FiniteFluid.LEVEL, capacity));
            this.playEmptySound(arg, level, pos);
            return true;
        } else {
            if (!level.isClientSide && flag && !blockstate.liquid()) {
                level.destroyBlock(pos, true);
            }

            BlockState newState = myFluid.defaultFluidState().createLegacyBlock()
                    .setValue(FlammableLiquidBlock.MISSING_LEVELS, 16 - capacity);
            if (!level.setBlock(pos, newState, 11) && !blockstate.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(arg, level, pos);
                return true;
            }
        }
    }

}
