package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.crafting.RecipeBookHack;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin extends RecipeBook {

    /*
    @Inject(method = "setupCollections",
            require = 1,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"))
    public void addSpecialRecipeDisplay(Iterable<Recipe<?>> recipes, CallbackInfo ci,
                                        Map<RecipeBookCategories, List<List<Recipe<?>>>> map){
        Map<RecipeBookCategories, List<List<Recipe<?>>>> extra = RecipeBookHack.createClientRecipes();
        if(!extra.isEmpty()){
            for(var v :  extra.entrySet()){
                var l = map.get(v.getKey());
                l.addAll(v.getValue());
                v.getValue().forEach(r->r.forEach(this::add));
            }
        }
    }*/
}
