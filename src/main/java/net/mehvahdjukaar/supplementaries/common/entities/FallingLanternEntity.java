package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FallingLanternEntity extends ImprovedFallingBlockEntity {

    public FallingLanternEntity(EntityType<FallingLanternEntity> type, Level level) {
        super(type, level);
    }

    public FallingLanternEntity(Level level) {
        super(ModRegistry.FALLING_LANTERN.get(), level);
    }

    public FallingLanternEntity(Level level, BlockPos pos, BlockState blockState, double yOffset) {
        super(ModRegistry.FALLING_LANTERN.get(),level,pos, blockState,false);
        this.yo = pos.getY() + yOffset;
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState state, double yOffset) {
        FallingLanternEntity entity = new FallingLanternEntity(level, pos, state, yOffset);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public boolean causeFallDamage(float height, float amount, DamageSource source) {
        boolean r = super.causeFallDamage(height, amount, source);
        if (ServerConfigs.cached.FALLING_LANTERNS.hasFire() && this.getDeltaMovement().lengthSqr() > 0.4*0.4) {
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
