package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.block.blocks.AshBlock;
import net.mehvahdjukaar.supplementaries.common.BlockItemUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FallingAshEntity extends FallingBlockEntity {

    public FallingAshEntity(EntityType<FallingAshEntity> type, Level level) {
        super(type, level);
    }

    public FallingAshEntity(Level level) {
        super(ModRegistry.FALLING_ASH.get(), level);
    }

    public FallingAshEntity(Level level, BlockPos pos, BlockState blockState) {
        this(level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY();
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + (double) ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(blockState);
        this.setHurtsEntities(1f, 20);
    }

    public void setBlockState(BlockState state) {
        //workaround
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Time", this.time);
        this.readAdditionalSaveData(tag);
    }


    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemLike pItem) {
        this.shatter();
        return null;
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();

        Block block = blockState.getBlock();
        if (this.time++ == 0) {
            BlockPos blockpos = this.blockPosition();
            if (this.level.getBlockState(blockpos).is(block)) {
                this.level.removeBlock(blockpos, false);
            } else if (!this.level.isClientSide) {
                this.discard();
                return;
            }
        }

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level.isClientSide) {

            BlockPos myPos = this.blockPosition();
            boolean isInWater = this.level.getFluidState(myPos).is(FluidTags.WATER);
            double d0 = this.getDeltaMovement().lengthSqr();
            if (d0 > 1.0D) {
                BlockHitResult blockhitresult = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                if (blockhitresult.getType() != HitResult.Type.MISS && this.level.getFluidState(blockhitresult.getBlockPos()).is(FluidTags.WATER)) {
                    myPos = blockhitresult.getBlockPos();
                    isInWater = true;
                }
            }

            if(isInWater){
                this.discard();
                return;
            }

            //fall
            if (!this.onGround) {
                if (!this.level.isClientSide && (this.time > 100 && (myPos.getY() <= this.level.getMinBuildHeight() || myPos.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
                    if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(block);
                    }

                    this.discard();
                }
                //on ground
            } else {
                BlockState onState = this.level.getBlockState(myPos);
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                if (!onState.is(Blocks.MOVING_PISTON)) {

                    this.discard();

                    ItemStack stack = blockState.getBlock().asItem().getDefaultInstance();

                    if (!FallingBlock.isFree(this.level.getBlockState(myPos.below()))) {

                        BlockPlaceContext context = new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND,stack,
                                new BlockHitResult(this.position(), Direction.UP, myPos, false));



                        BlockState placementState = BlockItemUtils.getPlacementState(
                                context, block);

                        //if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(myPos).getType() == Fluids.WATER) {
                        //    blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
                        //}
                        if (placementState != null && this.level.setBlock(myPos, placementState, 3)) {
                            BlockState placed = this.level.getBlockState(myPos);
                            ((ServerLevel) this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(myPos, placed));
                            if (block instanceof Fallable fallable) {
                                fallable.onLand(this.level, myPos, placed, onState, this);
                            }
                            return;
                        }
                    }
                    if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.callOnBrokenAfterFall(block, myPos);
                        this.spawnAtLocation(block);
                    }

                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));

    }

    private void shatter() {
        BlockState state = this.getBlockState();
        BlockPos pos = this.blockPosition();
        Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);


        level.levelEvent(null, 2001, pos, Block.getId(state));
    }
}
