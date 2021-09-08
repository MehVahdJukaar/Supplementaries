package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.mixins.accessors.MineshaftAccessor;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.MineshaftPieces;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(MineshaftPieces.Corridor.class)
public abstract class MineshaftMixin extends StructurePiece {


    protected MineshaftMixin(IStructurePieceType p_i51342_1_, int p_i51342_2_) {
        super(p_i51342_1_, p_i51342_2_);
    }

    private static final BlockState lantern = ModRegistry.COPPER_LANTERN.get().defaultBlockState().setValue(CopperLanternBlock.FACE, AttachFace.CEILING);
    private static final BlockState torch = Blocks.WALL_TORCH.defaultBlockState();

    protected boolean isSupportingBox(IBlockReader p_189918_1_, MutableBoundingBox p_189918_2_, int p_189918_3_, int p_189918_4_, int p_189918_5_, int p_189918_6_) {
        for(int i = p_189918_3_; i <= p_189918_4_; ++i) {
            if (this.getBlock(p_189918_1_, i, p_189918_5_ + 1, p_189918_6_, p_189918_2_).isAir()) {
                return false;
            }
        }
        return true;
    }


    /**
     * @author mehvahdjukaar
     * @reason replacing some torches with lanterns
     */

    @Overwrite
    private void placeSupport(ISeedReader reader, MutableBoundingBox boundingBox, int minX, int minY, int z, int y, int maxX, Random random) {
        if (this.isSupportingBox(reader, boundingBox, minX, maxX, y, z)) {
            BlockState plank = ((MineshaftAccessor) this).callGetPlanksBlock();
            BlockState fence = ((MineshaftAccessor) this).callGetFenceBlock();
            this.generateBox(reader, boundingBox, minX, minY, z, minX, y - 1, z, fence.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            this.generateBox(reader, boundingBox, maxX, minY, z, maxX, y - 1, z, fence.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if (random.nextInt(4) == 0) {
                this.generateBox(reader, boundingBox, minX, y, z, minX, y, z, plank, CAVE_AIR, false);
                this.generateBox(reader, boundingBox, maxX, y, z, maxX, y, z, plank, CAVE_AIR, false);
            } else {
                this.generateBox(reader, boundingBox, minX, y, z, maxX, y, z, plank, CAVE_AIR, false);

                if(RegistryConfigs.reg.HAS_MINESHAFT_LANTERN && 0.3>random.nextFloat()) {

                    //todo: add to SHAPE_CHECK_BLOCKS. finish this

                    boolean on = random.nextFloat()>0.2;

                    //if(!this.getBlock(reader, minX+1, y + 1, z-1, boundingBox).isAir())
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.06F, minX + 1, y, z - 1, lantern.setValue(WallLanternBlock.FACING, Direction.SOUTH)
                            .setValue(HorizontalFaceBlock.FACE,AttachFace.WALL).setValue(CopperLanternBlock.LIT,on));
                    //if(!this.getBlock(reader, minX+1, y + 1, z-1, boundingBox).isAir())
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.06F, minX + 1, y, z + 1, lantern.setValue(WallLanternBlock.FACING, Direction.NORTH)
                            .setValue(HorizontalFaceBlock.FACE,AttachFace.WALL).setValue(CopperLanternBlock.LIT,on));


                }
                else{
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.05F, minX + 1, y, z - 1, torch.setValue(WallTorchBlock.FACING, Direction.SOUTH));
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.05F, minX + 1, y, z + 1, torch.setValue(WallTorchBlock.FACING, Direction.NORTH));
                }
            }

        }
    }




}
