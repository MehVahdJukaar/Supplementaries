package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapedRecipe.class)
public class DumbFabricShapedRecipeMixin {

    @ModifyReturnValue(method = "itemStackFromJson", at = @At("RETURN"))
    private static ItemStack supp$fixFabricDumbNoNBTParsing(ItemStack original, @Local(argsOnly = true) JsonObject json) {
        if (json.has("nbt") && !original.hasTag()) {
            try {
                JsonElement nbt = json.get("nbt");
                CompoundTag tag = nbt.isJsonObject() ? TagParser.parseTag(nbt.toString()) : TagParser.parseTag(GsonHelper.convertToString(nbt, "nbt"));
                original.setTag(tag);
            } catch (Exception ignored) {
            }
        }
        return original;
    }

}
