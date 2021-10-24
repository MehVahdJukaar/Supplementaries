//package net.mehvahdjukaar.supplementaries.compat.inspirations;
//
//import knightminer.inspirations.library.recipe.RecipeSerializers;
//import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
//import knightminer.inspirations.library.recipe.cauldron.contents.ICauldronContents;
//import knightminer.inspirations.library.recipe.cauldron.special.DyeableCauldronRecipe;
//import knightminer.inspirations.library.recipe.cauldron.util.DisplayCauldronRecipe;
//import net.mehvahdjukaar.supplementaries.compat.jei.SupplementariesJEIPlugin;
//import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
//import net.minecraft.fluid.Fluids;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.crafting.IRecipeSerializer;
//import net.minecraft.item.crafting.Ingredient;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.util.ResourceLocation;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Stream;
//
///**
// * Recipe to clear a dyeable
// */
//public class CauldronBlackboardRecipe extends DyeableCauldronRecipe {
//    public CauldronBlackboardRecipe(ResourceLocation id, Ingredient ingredient) {
//        super(id, ingredient);
//    }
//
//    @Override
//    protected boolean matches(ICauldronContents contents, ItemStack stack) {
//        return contents.contains(CauldronContentTypes.FLUID, Fluids.WATER) && isDrawnBlackboard(stack);
//    }
//
//    public static boolean isDrawnBlackboard(ItemStack stack){
//        if (stack.getItem() == ModRegistry.BLACKBOARD_ITEM.get()) {
//            CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
//            return compoundnbt != null && compoundnbt.contains("Pixels");
//        }
//        return false;
//    }
//
//    @Override
//    protected ItemStack updateColor(ICauldronContents contents, ItemStack stack) {
//        stack.getTag().remove("BlockEntityTag");
//        return stack;
//    }
//
//    @Override
//    protected Stream<DisplayCauldronRecipe> getDisplayRecipes(ItemStack stack) {
//
//        List<ItemStack> inputs = Collections.singletonList(SupplementariesJEIPlugin.getSans());
//        return Stream.of(DisplayCauldronRecipe.builder(THIRD, 0)
//                .setItemInputs(inputs)
//                .setContentInputs(DisplayCauldronRecipe.WATER_CONTENTS.get())
//                .setItemOutput(stack)
//                .build());
//    }
//
//    @Override
//    public IRecipeSerializer<?> getSerializer() {
//        return RecipeSerializers.CAULDRON_CLEAR_DYEABLE;
//    }
//}