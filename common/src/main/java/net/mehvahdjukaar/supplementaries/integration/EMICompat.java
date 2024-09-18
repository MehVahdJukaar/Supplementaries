package net.mehvahdjukaar.supplementaries.integration;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@EmiEntrypoint
public class EMICompat implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        SpecialRecipeDisplays.registerCraftingRecipes(recipes -> recipes.stream().map(rh -> {
                    var r = rh.value();
                    return new EmiCraftingRecipe(
                            r.getIngredients().stream().map(EmiIngredient::of).toList(),
                            EmiStack.of(r.getResultItem(null)),
                            rh.id(),
                            r instanceof ShapelessRecipe);
                }
        ).forEach(registry::addRecipe));

        registry.addRecipe(
                EmiWorldInteractionRecipe.builder()
                        .id(Supplementaries.res("tilling/raked_gravel"))
                        .leftInput(EmiStack.of(Blocks.GRAVEL))
                        .rightInput(EmiStack.of(Items.IRON_HOE), true)
                        .output(EmiStack.of(ModRegistry.RAKED_GRAVEL.get()))
                        .build()
        );
        registry.addRecipe(new Grind(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE,
                Supplementaries.res("unenchanted_golden_apple")));
        registry.addRecipe(new Grind(ModRegistry.BOMB_BLUE_ITEM.get(), ModRegistry.BOMB_ITEM.get(),
                Supplementaries.res("unenchanted_golden_apple")));

        registry.addRecipe(
                EmiWorldInteractionRecipe.builder()
                        .id(Supplementaries.res("ash_burn"))
                        .leftInput(EmiIngredient.of(ItemTags.LOGS_THAT_BURN))
                        .rightInput(EmiStack.EMPTY, false, slotWidget ->
                                slotWidget.customBackground(ResourceLocation.parse("textures/block/stone.png"),
                                        0, 0, 256, 1))
                        .output(EmiStack.of(ModRegistry.ASH_BLOCK.get()))
                        .build()
        );


        registry.setDefaultComparison(EmiStack.of(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get()), Comparison.compareComponents());
    }

    public static class Grind implements EmiRecipe {
        private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/grindstone.png");
        private final ResourceLocation id;
        private final EmiStack to;
        private final EmiStack from;

        public Grind(Item from, Item to, ResourceLocation id) {
            this.id = id;
            this.from = EmiStack.of(from);
            this.to = EmiStack.of(to);
        }

        public EmiRecipeCategory getCategory() {
            return VanillaEmiRecipeCategories.GRINDING;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public List<EmiIngredient> getInputs() {
            return List.of(from);
        }

        public List<EmiStack> getOutputs() {
            return List.of(to);
        }

        public boolean supportsRecipeTree() {
            return false;
        }

        public int getDisplayWidth() {
            return 116;
        }

        public int getDisplayHeight() {
            return 56;
        }

        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(BACKGROUND, 0, 0, 116, 56, 30, 15);
            widgets.addText(this.getExp(), 114, 39, -1, true).horizontalAlign(TextWidget.Alignment.END);
            widgets.addSlot(from, 18, 3).drawBack(false);
            widgets.addSlot(to, 98, 18).drawBack(false).recipeContext(this);
        }

        private FormattedCharSequence getExp() {
            int minPower = 500;
            int minXP = (int) Math.ceil(minPower / 2.0);
            int maxXP = 2 * minXP - 1;
            return EmiPort.ordered(EmiPort.translatable("emi.grinding.experience", minXP, maxXP));
        }
    }

}