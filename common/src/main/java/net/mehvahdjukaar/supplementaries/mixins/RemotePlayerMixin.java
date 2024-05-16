package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin extends Player implements IQuiverPlayer {

    @Unique
    private SlotReference supplementaries$quiverSlotForHUD;
    @Unique
    private ItemStack supplementaries$quiverForRenderer;

    protected RemotePlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    //this isn't optimal but still better than checking every render tick the whole inventory
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
                    shift = At.Shift.AFTER)
    )
    private void checkIfHasQuiver(CallbackInfo ci) {
        supplementaries$quiverSlotForHUD = QuiverItem.getQuiverSlot(this);
        supplementaries$quiverForRenderer = supplementaries$quiverSlotForHUD.get();
    }

    @Override
    public SlotReference supplementaries$getQuiverSlot() {
        return supplementaries$quiverSlotForHUD;
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supplementaries$quiverForRenderer;
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
        this.supplementaries$quiverForRenderer = quiver;
    }
}