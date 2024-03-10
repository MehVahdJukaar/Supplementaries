package net.mehvahdjukaar.supplementaries.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

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
            SpecialRecipeDisplays.registerCraftingRecipes(r -> registry.addRecipes(RecipeTypes.CRAFTING, r));
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get(), SpikesSubtypeInterpreter.INSTANCE);
    }

    public  enum SpikesSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
        INSTANCE;

        public String apply(ItemStack itemStack, UidContext uidContext) {

            Potion potionType = PotionUtils.getPotion(itemStack);
            String potionTypeString = potionType.getName("");
            StringBuilder stringBuilder = new StringBuilder(potionTypeString);
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(itemStack);

            for (MobEffectInstance effect : effects) {
                stringBuilder.append(";").append(effect);
            }

            return stringBuilder.toString();
        }
    }

}