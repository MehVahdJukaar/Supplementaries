package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.components.LunchBaskedContent;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCarriedItem", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/world/entity/player/Inventory;selected:I"))
    private void supp$rerollLunchBasketOnSwap(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        Inventory inventory = this.player.getInventory();
        if (inventory.selected == packet.getSlot()) return;
        ItemStack stack = inventory.getItem(packet.getSlot());
        if (stack.getItem() instanceof LunchBoxItem basket) {
            LunchBaskedContent content = stack.get(basket.getComponentType());
            if (content != null && content.canEatFrom()) {
                basket.modify(stack, data -> SelectableContainerItem.applySelectionEnchantments(stack, data, player.getRandom()));
            }
        }
    }

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
