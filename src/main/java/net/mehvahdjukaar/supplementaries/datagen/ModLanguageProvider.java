package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.client.renderers.TextUtil;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String modId, String locale) {
        super(gen, modId, locale);
    }



    @Override
    protected void addTranslations() {
        for(IWoodType wood : WoodTypes.TYPES.values()){
            add(Registry.SIGN_POST_ITEMS.get(wood).get(), TextUtil.format(wood.toString()+"_"+Registry.SIGN_POST_NAME));
            add(Registry.HANGING_SIGNS_ITEMS.get(wood).get(), TextUtil.format(wood.toString()+"_"+Registry.HANGING_SIGN_NAME));
        }
    }



}
