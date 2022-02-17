package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.UrnBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FallingUrnEntity extends FallingBlockEntity {

    public FallingUrnEntity(EntityType<FallingUrnEntity> type, Level level) {
        super(type, level);
    }

    public FallingUrnEntity(Level level) {
        super(ModRegistry.FALLING_URN.get(), level);
    }

    public FallingUrnEntity(Level level, BlockPos pos, BlockState blockState) {
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

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemLike pItem) {
        this.shatter();
        return null;
    }

    @Override
    public boolean causeFallDamage(float height, float amount, DamageSource source) {
        boolean r = super.causeFallDamage(height, amount, source);
        if (this.getDeltaMovement().lengthSqr() > 0.5*0.5) {
            this.shatter();
            this.cancelDrop = true;
            this.discard();
        } else {
            if (!this.isSilent()) {
                level.levelEvent(1045, this.blockPosition(), 0);
            }
        }
        return r;
    }

    private void shatter() {
        BlockState state = this.getBlockState();
        CompoundTag tag = this.blockData;
        BlockEntity tile = null;
        BlockPos pos = this.blockPosition();
        if (tag != null && !tag.isEmpty()) {
            tile = new UrnBlockTile(pos, state);
            tile.load(tag);
        }
        Block.dropResources(state, level, pos, tile, null, ItemStack.EMPTY);

        level.levelEvent(null, 2001, pos, Block.getId(state));
        //todo: this needs to be called on client
        UrnBlock.spawnExtraBrokenParticles(state, pos, level);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        this.shatter();
        this.discard();
        return true;
    }
}
