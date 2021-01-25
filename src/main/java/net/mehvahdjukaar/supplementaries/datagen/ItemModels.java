package net.mehvahdjukaar.supplementaries.datagen;



import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {

    public ItemModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }


    @Override
    protected void registerModels() {
        for (IWoodType wood : WoodTypes.TYPES){
            //getBuilder(wood+"_beam").parent(new ModelFile.UncheckedModelFile(modLoc("block/"+wood+"_beam_y")));
            //getBuilder(wood+"_palisade").parent(new ModelFile.UncheckedModelFile(modLoc("block/"+wood+"_palisade_inventory")));
            //getBuilder(wood+"_seat").parent(new ModelFile.UncheckedModelFile(modLoc("block/"+wood+"_seat_inventory")));
            //getBuilder(wood+"_support").parent(new ModelFile.UncheckedModelFile(modLoc("block/"+wood+"_support")));
        }
    }


    private void makeHangingSignsItems(){
        for (IWoodType wood : WoodTypes.TYPES) {
           // withExistingParent(Registry.HANGING_SIGN_NAME+"-"+wood.toString(),modLoc("block"))
        }
    }



    @Override
    public String getName() {
        return "Decorative Block Item Models";
    }
}