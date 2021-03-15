package net.mehvahdjukaar.supplementaries.world.structures;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

//@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoadSignStructure extends Feature<NoFeatureConfig> {

    public RoadSignStructure(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Supplementaries.MOD_ID);

    public static final RegistryObject<Feature<NoFeatureConfig>> ROAD_SIGN = FEATURES.register("road_sign_struture",
            ()-> new RoadSignStructure(NoFeatureConfig.field_236558_a_));


    //@SubscribeEvent
    public static void addFeatureToBiomes(BiomeLoadingEvent event) {
        //.range(256).square().func_242731_b(50)
        event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> ROAD_SIGN.get()
                .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT)
                .range(256).chance(100)
                );
    }

    private static final BlockStateMatcher IS_SAND = BlockStateMatcher.forBlock(Blocks.SAND);
    private final BlockState sandSlab = Blocks.MOSSY_COBBLESTONE_SLAB.getDefaultState();
    private final BlockState sandstone = Blocks.DIAMOND_BLOCK.getDefaultState();
    private final BlockState water = Blocks.WATER.getDefaultState();
    public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        //if(!reader.getWorld().getServer().getServerConfiguration().getDimensionGeneratorSettings().doesGenerateFeatures()) return false;

        Structure<?> village = Structure.VILLAGE;
        if(true) return false;
        if(!(reader.getWorld() instanceof ServerWorld))return false;
        BlockPos villagePos =  reader.getWorld().func_241117_a_(village, pos, 50, false);
        for(pos = pos.up(); reader.isAirBlock(pos) && pos.getY() > 2; pos = pos.down()) {}


        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                if (reader.isAirBlock(pos.add(i, -1, j)) && reader.isAirBlock(pos.add(i, -2, j))) {
                    return false;
                }
            }
        }

        reader.setBlockState(pos.up(6), Registry.SIGN_POST.get().getDefaultState(), 3);
        TileEntity te = reader.getTileEntity(pos.up(6));
        if(te instanceof SignPostBlockTile){
            SignPostBlockTile sign = ((SignPostBlockTile) te);
            sign.setHeldBlock(Blocks.SPRUCE_FENCE.getDefaultState());
            sign.up=true;
            sign.yawUp = (float) (Math.atan2(villagePos.getX() - pos.getX(), villagePos.getZ() - pos.getZ()) * 180d / Math.PI);

        }


        for(int l = -1; l <= 0; ++l) {
            for(int l1 = -2; l1 <= 2; ++l1) {
                for(int k = -2; k <= 2; ++k) {
                    reader.setBlockState(pos.add(l1, l, k), this.sandSlab, 2);
                }
            }
        }

        reader.setBlockState(pos, this.water, 2);

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            reader.setBlockState(pos.offset(direction), this.water, 2);
        }

        for(int i1 = -2; i1 <= 2; ++i1) {
            for(int i2 = -2; i2 <= 2; ++i2) {
                if (i1 == -2 || i1 == 2 || i2 == -2 || i2 == 2) {
                    reader.setBlockState(pos.add(i1, 1, i2), this.sandstone, 2);
                }
            }
        }

        reader.setBlockState(pos.add(2, 1, 0), this.sandSlab, 2);
        reader.setBlockState(pos.add(-2, 1, 0), this.sandSlab, 2);
        reader.setBlockState(pos.add(0, 1, 2), this.sandSlab, 2);
        reader.setBlockState(pos.add(0, 1, -2), this.sandSlab, 2);

        for(int j1 = -1; j1 <= 1; ++j1) {
            for(int j2 = -1; j2 <= 1; ++j2) {
                if (j1 == 0 && j2 == 0) {
                    reader.setBlockState(pos.add(j1, 4, j2), this.sandstone, 2);
                } else {
                    reader.setBlockState(pos.add(j1, 4, j2), this.sandSlab, 2);
                }
            }
        }

        for(int k1 = 1; k1 <= 3; ++k1) {
            reader.setBlockState(pos.add(-1, k1, -1), this.sandstone, 2);
            reader.setBlockState(pos.add(-1, k1, 1), this.sandstone, 2);
            reader.setBlockState(pos.add(1, k1, -1), this.sandstone, 2);
            reader.setBlockState(pos.add(1, k1, 1), this.sandstone, 2);
        }

        return true;
    }

}
