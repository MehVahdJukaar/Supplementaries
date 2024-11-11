package net.mehvahdjukaar.supplementaries.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JEICompat implements IModPlugin {

    private static final ResourceLocation ID = Supplementaries.res("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if (!CompatHandler.REI && !CompatHandler.EMI) {
            SpecialRecipeDisplays.registerCraftingRecipes(r -> registry.addRecipes(RecipeTypes.CRAFTING,
                    (List<RecipeHolder<CraftingRecipe>>) (List) r));
        }
    }

    public  enum BuntingSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
        INSTANCE;

        public String apply(ItemStack itemStack, UidContext uidContext) {

            Optional<String> color = Optional.ofNullable(itemStack.getTag())
                    .map(tag -> tag.getString("Color"));

            return color.orElse("");
        }
    }

}