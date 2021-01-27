package net.mehvahdjukaar.supplementaries.datagen;


import net.mehvahdjukaar.supplementaries.common.Textures;
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
        for (IWoodType wood : WoodTypes.TYPES.values()) {
            makeSignPostItem(wood);
            makeHangingSignItem(wood);
        }
    }


    private void makeSignPostItem(IWoodType wood){
        getBuilder(Variants.getSignPostName(wood)).parent(new ModelFile.UncheckedModelFile(
                modLoc("item/sign_post_template")))
                .texture("0", Textures.SIGN_POSTS_TEXTURES.get(wood));

    }

    private void makeHangingSignItem(IWoodType wood){
        getBuilder(Variants.getHangingSignName(wood)).parent(new ModelFile.UncheckedModelFile(
                modLoc("item/hanging_sign_template")))
                .texture("0", "blocks/hanging_signs/"+wood.getLocation()+"hanging_sign_front_"+wood.toString())
                .texture("2", "blocks/hanging_signs/"+wood.getLocation()+"hanging_sign_details_"+wood.toString());

    }

}