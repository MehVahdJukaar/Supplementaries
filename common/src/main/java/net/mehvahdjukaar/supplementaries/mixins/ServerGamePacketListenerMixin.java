package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.function.UnaryOperator;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @ModifyArg(method = {"signBook", "updateBookContents"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;updateBookPages(Ljava/util/List;Ljava/util/function/UnaryOperator;Lnet/minecraft/world/item/ItemStack;)V"))
    private UnaryOperator<String> addAntiqueInk(List<FilteredText> list, UnaryOperator<String> unaryOperator, ItemStack itemStack) {
        if (AntiqueInkHelper.hasAntiqueInk(itemStack)) {
            return s -> {
                var c = Component.Serializer.fromJson(unaryOperator.apply(s));
                c = c.withStyle(c.getStyle().withFont(ModTextures.ANTIQUABLE_FONT));
                return Component.Serializer.toJson(c);
            };
        }
        return unaryOperator;
    }

}
