package net.mehvahdjukaar.supplementaries.datagen;



import net.mehvahdjukaar.supplementaries.block.CommonUtil;
import net.mehvahdjukaar.supplementaries.client.Textures;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
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
        //getBuilder("test").parent(getExistingFile(modLoc("item/generated"))).texture("layer0", "item/redstone");
        getBuilder("test").parent(new ModelFile.UncheckedModelFile(modLoc("block/cube")));
        getBuilder("test2");
    }


    private void makeHangingSignsItems(){

        for (IWoodType wood : WoodTypes.TYPES.values()) {
           //withExistingParent(,modLoc("sign_post_template"));

        }
    }



    @Override
    public String getName() {
        return "Decorative Block Item Models";
    }
}