package net.mehvahdjukaar.supplementaries.integration.fabric;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

public class EMICompat implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        SpecialRecipeDisplays.registerCraftingRecipes(recipes -> recipes.stream().map(r ->
                new EmiCraftingRecipe(
                        r.getIngredients().stream().map(EmiIngredient::of).toList(),
                        EmiStack.of(r.getResultItem()),
                        r.getId(),
                        r instanceof ShapelessRecipe
                )).forEach(registry::addRecipe));

        registry.addRecipe(
                EmiWorldInteractionRecipe.builder()
                        .id(new ResourceLocation(Supplementaries.MOD_ID, "tilling/raked_gravel"))
                        .leftInput(EmiStack.of(Blocks.GRAVEL))
                        .rightInput(EmiStack.of(Items.IRON_HOE), true)
                        .output(EmiStack.of(ModRegistry.RAKED_GRAVEL.get()))
                        .build()
        );

        registry.setDefaultComparison(EmiStack.of(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get()), c -> c.copy().nbt(true).build());
    }
}
