package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FallingLanternEntity extends FallingBlockEntity {

    public FallingLanternEntity(EntityType<FallingLanternEntity> type, Level level) {
        super(type, level);
    }

    public FallingLanternEntity(Level level) {
        super(ModRegistry.FALLING_LANTERN.get(), level);
    }

    public FallingLanternEntity(Level level, BlockPos pos, BlockState blockState, double yOffset) {
        this(level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY() + yOffset;
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + (double) ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(blockState);
    }

    public void setBlockState(BlockState state) {
        //workaround
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Time", this.time);
        this.readAdditionalSaveData(tag);
    }

    @Override
    public boolean causeFallDamage(float height, float amount, DamageSource source) {
        boolean r = super.causeFallDamage(height, amount, source);
        if (this.getDeltaMovement().lengthSqr() > 0.4*0.4) {
            BlockState state = this.getBlockState();

            BlockPos pos = this.blockPosition();
            level.levelEvent(null, 2001, pos, Block.getId(state));
            if(state.getLightEmission()!=0) {
                GunpowderBlock.createMiniExplosion(level, pos, true);
            }
            else{
                this.spawnAtLocation(state.getBlock());
            }
            this.cancelDrop =true;
            this.discard();
        }
        return r;
    }


}
