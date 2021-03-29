package net.mehvahdjukaar.supplementaries.world.structures.processors;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class SignDataProcessor extends StructureProcessor {

    public static final SignDataProcessor INSTANCE = new SignDataProcessor();
    public static final Codec<SignDataProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static IStructureProcessorType<SignDataProcessor> ROAD_SIGN_PROCESSOR = () -> CODEC;

    SignDataProcessor(){}

    @Override
    public Template.BlockInfo process(IWorldReader reader, BlockPos pos, BlockPos pos2, Template.BlockInfo structureBlockInfoLocal, Template.BlockInfo structureBlockInfoWorld, PlacementSettings structurePlacementData,  Template template) {
        //reader.get.setBlockState(structureBlockInfoWorld.pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), false);
        //reader.getChunk(pos).setBlockState(pos, Blocks.GOLD_BLOCK.defaultBlockState(), false);
        //reader.getChunk(pos2).setBlockState(pos2, Blocks.EMERALD_BLOCK.defaultBlockState(), false);
        //reader.getChunk(structureBlockInfoLocal.pos).setBlockState(structureBlockInfoLocal.pos, Blocks.BEACON.defaultBlockState(), false);

        //double dist = pos2.distSqr(structureBlockInfoWorld.pos);
        //Random r = new Random(pos.asLong());
        //Block b = r.nextFloat()<(dist/5f)?Blocks.DIAMOND_BLOCK:Blocks.EMERALD_BLOCK;

        return structureBlockInfoWorld;
    }


    @Override
    protected IStructureProcessorType<?> getType() {
        return ROAD_SIGN_PROCESSOR;

    }

    public static void register(){
        Registry.register(Registry.STRUCTURE_PROCESSOR, new ResourceLocation(Supplementaries.MOD_ID, "data_processor"), ROAD_SIGN_PROCESSOR);
    }
}