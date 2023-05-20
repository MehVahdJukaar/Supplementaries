package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.IHangingSignExtension;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.BlockAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WallHangingSignBlock.class)
public abstract class WallHangingSignBlockMixin extends Block implements EntityBlock {

    protected WallHangingSignBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "updateShape", at = @At("HEAD"))
    public void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (level.getBlockEntity(currentPos) instanceof IHangingSignExtension tile) {
            tile.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
    }

    @Inject(method = "canAttachTo",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"),
            cancellable = true)
    public void canAttachTo(LevelReader level, BlockState state, BlockPos facingPos, Direction direction,
                            CallbackInfoReturnable<Boolean> cir, BlockState facingState) {
        if (BlockAttachment.get(facingState, facingPos, level, direction) != null) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof IHangingSignExtension tile) {
            tile.updateAttachments();
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide && level.getBlockEntity(pos) instanceof IHangingSignExtension tile) {
            tile.getSwayingAnimation().hitByEntity(entity, state, pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, BlockEntityType.HANGING_SIGN, pLevel.isClientSide ? (level, blockPos, blockState, blockEntity) ->
                ((IHangingSignExtension) blockEntity).getSwayingAnimation().tick(level, blockPos, blockState) : null);
    }


}
