package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.ILevelEventRedirect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ILevelEventRedirect {

    @Shadow
    @Nullable
    public abstract Entity getEntity(int pId);

    @Unique
    private boolean redirectLevelEvents = false;
    @Unique
    private Vec3 redirectedEntityPos = Vec3.ZERO;

    protected ServerLevelMixin(WritableLevelData p_204149_, ResourceKey<Level> p_204150_, Holder<DimensionType> p_204151_, Supplier<ProfilerFiller> p_204152_, boolean p_204153_, boolean p_204154_, long p_204155_) {
        super(p_204149_, p_204150_, p_204151_, p_204152_, p_204153_, p_204154_, p_204155_);
    }

    @Override
    public void setRedirected(boolean redirected, Vec3 id) {
        this.redirectLevelEvents = redirected;
        this.redirectedEntityPos = id;
    }

    //for dispenser minecart
    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData, CallbackInfo ci) {
        if (this.redirectLevelEvents && ILevelEventRedirect.tryRedirect(this, pPlayer, redirectedEntityPos, pType, pPos, pData)) {
            ci.cancel();
        }
    }
}
