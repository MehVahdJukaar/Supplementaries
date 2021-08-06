package net.mehvahdjukaar.supplementaries.compat.inspirations;

import knightminer.inspirations.library.recipe.RecipeSerializers;
import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
import knightminer.inspirations.library.recipe.cauldron.inventory.ICauldronInventory;
import knightminer.inspirations.library.recipe.cauldron.inventory.IModifyableCauldronInventory;
import knightminer.inspirations.library.recipe.cauldron.recipe.ICauldronRecipe;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class CauldronMapClearRecipe implements ICauldronRecipe {
    private final ResourceLocation id;
    public CauldronMapClearRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(ICauldronInventory inv, World worldIn) {
        ItemStack stack = inv.getStack();
        // must be at least one level of water, be a banner, and have patterns
        return inv.getLevel() >= THIRD && inv.getContents().contains(CauldronContentTypes.FLUID, Fluids.WATER)
                && stack.getItem() instanceof FilledMapItem;
    }

    @Override
    public void handleRecipe(IModifyableCauldronInventory inv) {
        // remove patterns
        ItemStack stack = inv.getStack();

        MapData data = FilledMapItem.getOrCreateSavedData(stack,null);
        if(data instanceof CustomDecorationHolder) {
            ((CustomDecorationHolder) data).resetCustomDecoration();
        }


        // use one level of water
        inv.addLevel(-THIRD);

        // play sound
        inv.playSound(SoundEvents.GENERIC_SPLASH);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeSerializers.CAULDRON_REMOVE_BANNER_PATTERN;
    }

}