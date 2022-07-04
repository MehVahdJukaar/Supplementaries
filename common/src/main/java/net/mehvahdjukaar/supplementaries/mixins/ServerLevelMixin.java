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

    protected ServerLevelMixin(WritableLevelData p_220352_, ResourceKey<Level> p_220353_, Holder<DimensionType> p_220354_, Supplier<ProfilerFiller> p_220355_, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_) {
        super(p_220352_, p_220353_, p_220354_, p_220355_, p_220356_, p_220357_, p_220358_, p_220359_);
    }

    @Shadow
    @Nullable
    public abstract Entity getEntity(int pId);

    @Unique
    private boolean redirectLevelEvents = false;
    @Unique
    private Vec3 redirectedEntityPos = Vec3.ZERO;


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
