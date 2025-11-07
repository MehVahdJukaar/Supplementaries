package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    //TODO: add back or remove
    /*
    @ModifyArg(method = {"signBook", "updateBookContents"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;updateBookPages(Ljava/util/List;Ljava/util/function/UnaryOperator;Lnet/minecraft/world/item/ItemStack;)V"))
    private UnaryOperator<String> addAntiqueInk(List<FilteredText> list, UnaryOperator<String> unaryOperator, ItemStack itemStack) {
        if (AntiqueInkItem.hasAntiqueInk(itemStack)) {
            return s -> {
                var c = Component.Serializer.fromJson(unaryOperator.apply(s));
                c = c.withStyle(c.getStyle().withFont(ModTextures.ANTIQUABLE_FONT));
                return Component.Serializer.toJson(c);
            };
        }
        return unaryOperator;
    }*/

}
