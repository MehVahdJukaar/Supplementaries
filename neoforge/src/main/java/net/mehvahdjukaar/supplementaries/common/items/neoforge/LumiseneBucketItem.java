package net.mehvahdjukaar.supplementaries.common.items.neoforge;

import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class LumiseneBucketItem extends BucketItem {
    private final int capacity;

    public LumiseneBucketItem(Supplier<? extends FiniteFluid> supplier, Properties builder, int capacity) {
        super(supplier.get(), builder);
        this.capacity = capacity;
    }

    // we override this JUSt so we can place on water

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        VibeChecker.assertSameLevel(level, player);
        ItemStack itemstack = player.getItemInHand(usedHand);

        // changed here
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos hitPos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos above = hitPos.relative(direction);
            //manual check since we manually raytrace


            if (!level.mayInteract(player, hitPos) || !player.mayUseItemAt(above, direction, itemstack)) {
                return InteractionResultHolder.fail(itemstack);
            } else {
                BlockState blockstate = level.getBlockState(hitPos);
                BlockPos blockpos2 = this.canBlockContainFluid(player, level, hitPos, blockstate) ? hitPos : above;
                if (this.emptyContents(player, level, blockpos2, blockhitresult, itemstack)) {
                    this.checkExtraContent(player, level, itemstack, blockpos2);
                    if (player instanceof ServerPlayer sp) {
                        CriteriaTriggers.PLACED_BLOCK.trigger(sp, blockpos2, itemstack);
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
    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult hitResult, @Nullable ItemStack container) {
        BlockState stateAt = level.getBlockState(pos);
        Block blockAt = stateAt.getBlock();
        boolean canReplaceBlock = stateAt.canBeReplaced(content);
        boolean canFillBlock = stateAt.isAir() || canReplaceBlock || blockAt instanceof LiquidBlockContainer lc &&
                lc.canPlaceLiquid(player, level, pos, stateAt, content);
        Optional<FluidStack> containedFluidStack = Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);
        if (!canFillBlock) {
            //try above
            return hitResult != null && this.emptyContents(player, level, hitResult.getBlockPos().relative(hitResult.getDirection()), null, container);
        } else if (containedFluidStack.isPresent() && content.getFluidType().isVaporizedOnPlacement(level, pos, containedFluidStack.get())) {
            content.getFluidType().onVaporize(player, level, pos, containedFluidStack.get());
            return true;
        } if (blockAt instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(player, level, pos, stateAt, content)) {
            lc.placeLiquid(level, pos, stateAt, content.defaultFluidState()
                    .setValue(FiniteFluid.LEVEL, capacity));
            this.playEmptySound(player, level, pos);
            return true;
        } else {
            if (!level.isClientSide && canReplaceBlock && !stateAt.liquid()) {
                level.destroyBlock(pos, true);
            }

            BlockState newState = content.defaultFluidState().createLegacyBlock()
                    .setValue(FlammableLiquidBlock.MISSING_LEVELS, 16 - capacity);
            if (!level.setBlock(pos, newState, 11) && !stateAt.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(player, level, pos);
                return true;
            }
        }
    }

}
