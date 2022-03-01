package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.IPlaceableItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Item.class)
public class ItemMixin implements IPlaceableItem {

    @Unique
    @Nullable
    private BlockItem placeableItem;

    @Shadow
    @Final
    @Nullable
    private FoodProperties foodProperties;

    //delegates stuff to internal blockItem
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.placeableItem != null) {
            cir.setReturnValue(this.placeableItem.useOn(pContext));
        }
    }

    @Override
    public BlockItem getBlockItemOverride() {
        return placeableItem;
    }

    @Override
    public void addPlaceable(Block block) {
        this.placeableItem = new BlockItem(block, new Item.Properties().food(this.foodProperties));
        regBlocksInternal(this);
        // Item.BY_BLOCK.put(this.placeableItem.getBlock(), this);
    }

    private void regBlocksInternal(Object item) {
        var map = GameData.getBlockItemMap();
        this.placeableItem.registerBlocks(map, (Item) item);
    }

    //delegates stuff to internal blockItem
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced, CallbackInfo ci) {
        if (this.placeableItem != null) {
            pTooltipComponents.add(new TranslatableComponent("message.supplementaries.placeable").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }

}
