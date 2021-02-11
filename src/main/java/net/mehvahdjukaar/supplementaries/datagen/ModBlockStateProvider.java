package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for(IWoodType wood : WoodTypes.TYPES.values()) {
            makeHangingSignsBlock(wood);
        }
    }


    private ModelFile hangingSignModel(IWoodType wood, String baseName, String type){
        return models().getBuilder(baseName+"_"+type+"_"+wood.getRegName())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/"+baseName+"_"+type+"_template")))
                .texture("particle", "blocks/hanging_signs/"+wood.getLocation()+"hanging_sign_front_"+wood.getRegName())
                .texture("0", "blocks/hanging_signs/"+wood.getLocation()+"hanging_sign_front_"+wood.getRegName())
                .texture("1", "blocks/hanging_signs/"+wood.getLocation()+"hanging_sign_details_"+wood.getRegName());
    }

    private void makeHangingSignsBlock(IWoodType wood){

        getVariantBuilder(Registry.HANGING_SIGNS.get(wood).get()).forAllStatesExcept(state -> {
            String baseName = Registry.HANGING_SIGN_NAME;

            ModelFile model = hangingSignModel(wood,baseName,state.get(HangingSignBlock.TILE)?"tile":
                    state.get(HangingSignBlock.EXTENSION).toString());

            return ConfiguredModel.builder().modelFile(model)
                    .rotationY(((int) state.get(BlockStateProperties.HORIZONTAL_FACING).getHorizontalAngle() + 180) % 360)
                    .build();
        }, HangingSignBlock.HANGING, HangingSignBlock.WATERLOGGED);

    }

}
