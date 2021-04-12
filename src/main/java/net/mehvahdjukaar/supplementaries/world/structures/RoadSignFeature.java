package net.mehvahdjukaar.supplementaries.world.structures;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.block.blocks.StructureTempBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;


public class RoadSignFeature extends Feature<NoFeatureConfig> {

    public RoadSignFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }


    private final BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
    private final BlockState mossyCobble = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private final BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();

    private final BlockState wall = Blocks.COBBLESTONE_WALL.defaultBlockState();
    private final BlockState mossyWall = Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();

    private static boolean canGoThrough(IWorld world, BlockPos pos) {
        if (!world.getFluidState(pos).isEmpty()) return false;

        return world.isStateAtPosition(pos, (state) ->{
            Material material = state.getMaterial();
            return material.isReplaceable() || material==Material.LEAVES || material==Material.PLANT;});
    }


    public static boolean isReplaceable(IWorld world, BlockPos pos) {

        return world.isStateAtPosition(pos, (state) ->{
                if(state.getBlock() instanceof StructureTempBlock)return true;
                Material material = state.getMaterial();
                return material.isReplaceable() && material!=Material.LEAVES;});
    }

    public static boolean isNotSolid(IWorld world, BlockPos pos){
        return !world.isStateAtPosition(pos, (state) -> state.isRedstoneConductor(world, pos));
    }




    @Override
    public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {

        /*
        if(!reader.getLevel().dimension().equals(World.OVERWORLD))return false;
        if(pos.getY()>90 || pos.getY()<50)return false;
        if(!reader.getLevel().getChunkSource().generator.getBiomeSource().canGenerateStructure(Structure.VILLAGE))return false;


        //find nearest solid block
        for(pos = pos.above(); canGoThrough(reader,pos) && pos.getY() > 2; pos = pos.below()) {}

        if(isNotSolid(reader, pos))return false;


        for(int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                //checks for empty blocks around wall
                for(int h = 2; h<=5; h++) {
                    //skip angles
                    //if(Math.abs(i)==2&&Math.abs(j)==2)continue;
                    if (!isReplaceable(reader,pos.offset(i,h,j))) {
                        return false;
                    }
                }
                //allows 1 block of leaves at the base
                if (!canGoThrough(reader,pos.offset(i,1,j)))return false;
                //thick solid base. no floaty sings here
                if(isNotSolid(reader, pos.offset(i, 0, j)))return false;
                if(isNotSolid(reader, pos.offset(i, -1, j)))return false;
                //if(isNotSolid(reader, pos.offset(i, -2, j)))return false;
            }
        }*/

        //for jigsaw
        pos = pos.below();

        //add air blocks around
        for(int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                if(Math.abs(i)==2&&Math.abs(j)==2)continue;
                for (int k = 1; k <= 4; ++k) {
                    if((Math.abs(i)==2||Math.abs(j)==2)&&k==1)continue;
                    reader.setBlock(pos.offset(i, k, j),Registry.STRUCTURE_TEMP.get().defaultBlockState(), 2);
                }
            }
        }

        float humidity = reader.getBiome(pos).getDownfall();


        //generate cobble path
        for(int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if(Math.abs(i)==2&&Math.abs(j)==2)continue;
                reader.setBlock(pos.offset(i, -1, j),this.cobble, 2);

                BlockPos pathPos = pos.offset(i, 0, j);
                double dist = pos.distSqr(pathPos.getX(),pathPos.getY(),pathPos.getZ(),false)/5.2f;

                if(rand.nextFloat()<dist-0.15)continue;
;
                boolean m = (humidity*0.75)>rand.nextFloat();
                reader.setBlock(pathPos, m?this.mossyCobble:this.cobble, 2);
            }
        }


        //post

        boolean m = (humidity*0.75)>rand.nextFloat();

        pos = pos.above();
        reader.setBlock(pos, m?this.mossyWall:this.wall, 2);
        pos = pos.above();
        reader.setBlock(pos, this.fence,2);
        pos = pos.above();
        reader.setBlock(pos, this.fence,2);
        reader.setBlock(pos.above(), Registry.BLOCK_GENERATOR.get().defaultBlockState(),2);



        return true;
    }


}
