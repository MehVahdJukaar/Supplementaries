//package net.mehvahdjukaar.supplementaries.compat.inspirations;
//
//import knightminer.inspirations.library.recipe.RecipeSerializers;
//import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
//import knightminer.inspirations.library.recipe.cauldron.inventory.ICauldronInventory;
//import knightminer.inspirations.library.recipe.cauldron.inventory.IModifyableCauldronInventory;
//import knightminer.inspirations.library.recipe.cauldron.recipe.ICauldronRecipe;
//import knightminer.inspirations.library.recipe.cauldron.util.DisplayCauldronRecipe;
//import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
//import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
//import net.minecraft.fluid.Fluids;
//import net.minecraft.item.BannerPatternItem;
//import net.minecraft.item.DyeColor;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.item.crafting.IRecipeSerializer;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.nbt.ListNBT;
//import net.minecraft.tileentity.BannerTileEntity;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.SoundEvents;
//import net.minecraft.world.World;
//import slimeknights.mantle.recipe.IMultiRecipe;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CauldronFlagClearRecipe implements ICauldronRecipe, IMultiRecipe<DisplayCauldronRecipe> {
//    private final ResourceLocation id;
//    public CauldronFlagClearRecipe(ResourceLocation id) {
//        this.id = id;
//    }
//
//    @Override
//    public ResourceLocation getId() {
//        return id;
//    }
//
//    @Override
//    public boolean matches(ICauldronInventory inv, World worldIn) {
//        ItemStack stack = inv.getStack();
//        // must be at least one level of water, be a banner, and have patterns
//        return inv.getLevel() >= THIRD && inv.getContents().contains(CauldronContentTypes.FLUID, Fluids.WATER)
//                && stack.getItem() instanceof FlagItem
//                && BannerTileEntity.getPatternCount(stack) > 0;
//    }
//
//    private List<DisplayCauldronRecipe> displayRecipes;
//    @Override
//    public List<DisplayCauldronRecipe> getRecipes() {
//        if (this.displayRecipes == null) {
//            this.displayRecipes = new ArrayList<>();
//
//
//            //List<BannerPatternItem> patterns = ForgeRegistries.ITEMS.getValues().stream().filter(i -> i instanceof BannerPatternItem)
//            //        .map(i -> (BannerPatternItem)i).collect(Collectors.toList());
//
//            for (DyeColor color : DyeColor.values()) {
//
//                //for(BannerPatternItem i : patterns) {
//                    ItemStack stack = new ItemStack(ModRegistry.FLAGS.get(color).get());
//
//                    CompoundNBT com = stack.getOrCreateTagElement("BlockEntityTag");
//                    ListNBT list = new ListNBT();
//                    CompoundNBT compoundnbt1 = new CompoundNBT();
//                    compoundnbt1.putString("Pattern", ((BannerPatternItem)Items.SKULL_BANNER_PATTERN).getBannerPattern().getHashname());
//                    compoundnbt1.putInt("Color", color == DyeColor.WHITE ? DyeColor.BLACK.getId() : DyeColor.WHITE.getId());
//                    list.add(compoundnbt1);
//                    com.put("Patterns", list);
//
//
//                    displayRecipes.add(DisplayCauldronRecipe.builder(THIRD, 0)
//                            .setItemInputs(stack)
//                            .setContentInputs(CauldronContentTypes.FLUID.of(Fluids.WATER))
//                            .setItemOutput(new ItemStack(ModRegistry.FLAGS.get(color).get()))
//                            .build());
//                }
//            //}
//        }
//        return this.displayRecipes;
//    }
//
//
//
//
//
//    @Override
//    public void handleRecipe(IModifyableCauldronInventory inv) {
//        // remove patterns
//        ItemStack stack = inv.splitStack(1);
//        BannerTileEntity.removeLastPattern(stack);
//        inv.setOrGiveStack(stack);
//        // use one level of water
//        inv.addLevel(-THIRD);
//
//        // play sound
//        inv.playSound(SoundEvents.GENERIC_SPLASH);
//    }
//
//    @Override
//    public IRecipeSerializer<?> getSerializer() {
//        return RecipeSerializers.CAULDRON_REMOVE_BANNER_PATTERN;
//    }
//
//}