package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.IFluteParrot;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Parrot.class)
public abstract class ParrotMixin extends Entity implements IFluteParrot {

    @Shadow
    private boolean partyParrot;
    @Shadow
    @Nullable
    private BlockPos jukebox;

    @Unique
    private final List<Player> fluteEntities = new ArrayList<>();

    public ParrotMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void supplementaries$fluteAi(CallbackInfo ci) {
        if (!fluteEntities.isEmpty()) {

            fluteEntities.removeIf(player -> player.isRemoved() ||
                    !player.blockPosition().closerToCenterThan(this.position(), 3.46)
                    || !(player.getUseItem().getItem() instanceof FluteItem));
            if (!fluteEntities.isEmpty()) partyParrot = true;
            else if (jukebox == null && partyParrot) partyParrot = false;
        }
    }

    @Override
    public void supplementaries$setPartyByFlute(Player player) {
        fluteEntities.add(player);
        partyParrot = true;
    }
}
