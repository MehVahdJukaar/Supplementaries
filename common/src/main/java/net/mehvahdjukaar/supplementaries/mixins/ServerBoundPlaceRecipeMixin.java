package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.crafting.RecipeBookHack;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundPlaceRecipePacket.class)
public abstract class ServerBoundPlaceRecipeMixin {

    @Shadow
    @Final
    private ResourceLocation recipe;

    @Shadow
    @Final
    private int containerId;

    @Shadow
    @Final
    private boolean shiftDown;

    @Inject(method = "handle(Lnet/minecraft/network/PacketListener;)V",
            at = @At("TAIL"))
    public void handleSpecialRecipeDisplays(PacketListener handler, CallbackInfo ci) {
        var v = RecipeBookHack.getSpecialRecipe(this.recipe);
        if (v != null && handler instanceof ServerGamePacketListenerImpl sp) {
            ServerPlayer player = sp.player;
            if (!player.isSpectator() && player.containerMenu.containerId == this.containerId && player.containerMenu instanceof RecipeBookMenu<?> rm) {
                rm.handlePlacement(this.shiftDown, v, player);
            }
        }
    }
}
