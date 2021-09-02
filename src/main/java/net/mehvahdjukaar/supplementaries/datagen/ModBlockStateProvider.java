package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
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
            //makeHangingSignsBlock(wood);
        }
        for(DyeColor color :DyeColor.values()){
            makePresentBlock(color);
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

        getVariantBuilder(ModRegistry.HANGING_SIGNS.get(wood).get()).forAllStatesExcept(state -> {
            String baseName = ModRegistry.HANGING_SIGN_NAME;

            ModelFile model = hangingSignModel(wood,baseName,state.getValue(HangingSignBlock.TILE)?"tile":
                    state.getValue(HangingSignBlock.EXTENSION).toString());

            return ConfiguredModel.builder().modelFile(model)
                    .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                    .build();
        }, HangingSignBlock.HANGING, HangingSignBlock.WATERLOGGED);

    }

    private void makePresentBlock(DyeColor color){

        getVariantBuilder(ModRegistry.PRESENTS.get(color).get()).forAllStatesExcept(state -> {
            String baseName = ModRegistry.PRESENT_NAME;

            ModelFile model = presentModel(color, baseName,state.getValue(PresentBlock.OPEN)?"opened": "closed");

            return ConfiguredModel.builder().modelFile(model)
                    .build();
        }, PresentBlock.WATERLOGGED);

    }

    private ModelFile presentModel(DyeColor color, String baseName, String type){
        return models().getBuilder(baseName+"_"+type+"_"+color.getName())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/"+baseName+"_"+type+"_template")))
                .texture("particle", "blocks/presents/present_side_"+color.getName())
                .texture("side", "blocks/presents/present_side_"+color.getName())
                .texture("top", "blocks/presents/present_top_"+color.getName())
                .texture("bottom", "blocks/presents/present_bottom_"+color.getName())
                .texture("inside", "blocks/presents/present_inside_"+color.getName());
    }

}
