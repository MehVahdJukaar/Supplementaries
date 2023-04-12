package net.mehvahdjukaar.supplementaries.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.RecipeBookHack;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin extends RecipeBook {

    @Inject(method = "setupCollections",
            require = 1,
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"))
    public void addSpecialRecipeDisplay(Iterable<Recipe<?>> recipes, CallbackInfo ci,
                                        Map<RecipeBookCategories, List<List<Recipe<?>>>> map){
        Map<RecipeBookCategories, List<List<Recipe<?>>>> extra = RecipeBookHack.createSpecialRecipeDisplays();
        if(!extra.isEmpty()){
            for(var v :  extra.entrySet()){
                var l = map.get(v.getKey());
                l.addAll(v.getValue());
                v.getValue().forEach(r->r.forEach(this::add));
            }
        }
    }
}
