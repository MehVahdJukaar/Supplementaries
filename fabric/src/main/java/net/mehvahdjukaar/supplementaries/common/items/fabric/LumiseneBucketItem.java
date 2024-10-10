package net.mehvahdjukaar.supplementaries.common.items.fabric;

import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class LumiseneBucketItem extends BucketItem {
    private final int capacity;
    private final Fluid content;

    public LumiseneBucketItem(Fluid content, Properties properties, int capacity) {
        super(content, properties);
        this.capacity = capacity;
        this.content = content;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        // changed here

        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        } else if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        } else {
            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos above = hitPos.relative(direction);
            if (level.mayInteract(player, hitPos) && player.mayUseItemAt(above, direction, itemStack)) {
                BlockState blockState;

                blockState = level.getBlockState(hitPos);
                BlockPos blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? hitPos : above;
                if (this.emptyContents(player, level, blockPos3, blockHitResult)) {
                    this.checkExtraContent(player, level, itemStack, blockPos3);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos3, itemStack);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemStack, player), level.isClientSide());
                } else {
                    return InteractionResultHolder.fail(itemStack);
                }

            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }


    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result) {
        BlockState blockStateAt = level.getBlockState(pos);
        Block blockAt = blockStateAt.getBlock();
        boolean canReplaceBlock = blockStateAt.canBeReplaced(this.content);
        boolean canPlaceLiquidIn = blockStateAt.isAir() || canReplaceBlock || blockAt instanceof LiquidBlockContainer lc && lc.canPlaceLiquid(
                player, level, pos, blockStateAt, this.content);
        if (!canPlaceLiquidIn) {
            return result != null && this.emptyContents(player, level, result.getBlockPos().relative(result.getDirection()), null);
        } else {
            if (!level.isClientSide && canReplaceBlock && !blockStateAt.liquid()) {
                level.destroyBlock(pos, true);
            }

            if (!level.setBlock(pos, this.content.defaultFluidState().createLegacyBlock()
                    .setValue(FlammableLiquidBlock.MISSING_LEVELS, 16 - capacity), 11)
                    && !blockStateAt.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(player, level, pos);
                return true;
            }
        }
    }

}
