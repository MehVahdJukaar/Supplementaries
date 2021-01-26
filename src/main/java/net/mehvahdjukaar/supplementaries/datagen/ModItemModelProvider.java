package net.mehvahdjukaar.supplementaries.datagen;


import net.mehvahdjukaar.supplementaries.client.Textures;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.registration.Variants;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }


    @Override
    protected void registerModels() {
        makeSignPostItems();
    }


    private void makeSignPostItems(){
        for (IWoodType wood : WoodTypes.TYPES.values()) {
            getBuilder(Variants.getSignPostName(wood)).parent(new ModelFile.UncheckedModelFile(
                    modLoc("item/sign_post_template")))
                    .texture("0", Textures.SIGN_POSTS_TEXTURES.get(wood));
        }
    }

    @Override
    public String getName() {
        return "generated_"+modid+"_items";
    }
}