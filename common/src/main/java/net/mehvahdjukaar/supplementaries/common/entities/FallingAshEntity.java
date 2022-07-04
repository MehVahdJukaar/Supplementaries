package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.impl.entities.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FallingAshEntity extends ImprovedFallingBlockEntity {

    public FallingAshEntity(EntityType<FallingAshEntity> type, Level level) {
        super(type, level);
    }

    public FallingAshEntity(Level level) {
        super(ModRegistry.FALLING_ASH.get(), level);
    }

    public FallingAshEntity(Level level, BlockPos pos, BlockState blockState) {
        super(ModRegistry.FALLING_ASH.get(), level, pos, blockState, false);
    }

    public static FallingAshEntity fall(Level level, BlockPos pos, BlockState state) {
        FallingAshEntity entity = new FallingAshEntity(level, pos, state);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemLike pItem) {
        this.dropBlockContent(this.getBlockState(), this.blockPosition());
        return null;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        if (level.isClientSide) {
            super.tick();
            return;
        }
        BlockState blockState = this.getBlockState();
        if (blockState.isAir()) {
            this.discard();
        } else {
            Block block = blockState.getBlock();
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level.isClientSide) {
                BlockPos pos = this.blockPosition();
                if (this.level.getFluidState(pos).is(FluidTags.WATER)) {
                    this.discard();
                    return;
                }
                if (this.getDeltaMovement().lengthSqr() > 1.0D) {
                    BlockHitResult blockhitresult = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                    if (blockhitresult.getType() != HitResult.Type.MISS && this.level.getFluidState(blockhitresult.getBlockPos()).is(FluidTags.WATER)) {
                        this.discard();
                        return;
                    }
                }


                //fall
                if (!this.onGround) {
                    if (!this.level.isClientSide && (this.time > 100 && (pos.getY() <= this.level.getMinBuildHeight() || pos.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
                        if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }

                        this.discard();
                    }
                    //place
                } else {
                    BlockState onState = this.level.getBlockState(pos);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                    if (!onState.is(Blocks.MOVING_PISTON)) {

                        boolean canBeReplaced = onState.canBeReplaced(new DirectionalPlaceContext(this.level, pos, Direction.DOWN,
                                new ItemStack(blockState.getBlock().asItem()), Direction.UP));
                        boolean isFree = isFree(this.level.getBlockState(pos.below()));
                        boolean canSurvive = blockState.canSurvive(this.level, pos) && !isFree;
                        if (canBeReplaced && canSurvive) {

                            int remaining = 0;

                            if (onState.is(blockState.getBlock())) {
                                int layers = blockState.getValue(AshLayerBlock.LAYERS);
                                int toLayers = onState.getValue(AshLayerBlock.LAYERS);
                                int total = layers + toLayers;
                                int target = Mth.clamp(total, 1, 8);
                                remaining = total - target;
                                blockState = blockState.setValue(AshLayerBlock.LAYERS, target);
                            }

                            if (this.level.setBlock(pos, blockState, 3)) {
                                ((ServerLevel) this.level).getChunkSource().chunkMap.broadcast(this,
                                        new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));

                                if (block instanceof Fallable fallable) {
                                    fallable.onLand(this.level, pos, blockState, onState, this);
                                }
                                this.discard();

                                if (remaining != 0) {
                                    BlockPos above = pos.above();
                                    blockState = blockState.setValue(AshLayerBlock.LAYERS, remaining);
                                    if (level.getBlockState(above).getMaterial().isReplaceable()) {
                                        if (!this.level.setBlock(above, blockState, 3)) {
                                            ((ServerLevel) this.level).getChunkSource().chunkMap.broadcast(this,
                                                    new ClientboundBlockUpdatePacket(above, this.level.getBlockState(above)));
                                            this.dropBlockContent(blockState, pos);
                                        }
                                    }
                                }


                                return;
                            }
                        }
                        this.discard();
                        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.callOnBrokenAfterFall(block, pos);
                            this.dropBlockContent(blockState, pos);
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }

    public static boolean isFree(BlockState pState) {
        Material material = pState.getMaterial();
        return pState.isAir() || pState.is(BlockTags.FIRE) || material.isLiquid() || (material.isReplaceable() && !(pState.getBlock() instanceof AshLayerBlock));
    }


    private void dropBlockContent(BlockState state, BlockPos pos) {
        Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);

        level.levelEvent(null, 2001, pos, Block.getId(state));
    }
}
