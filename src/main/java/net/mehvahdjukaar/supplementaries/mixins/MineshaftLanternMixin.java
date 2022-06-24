package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.mixins.accessors.MineshaftAccessor;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MineshaftPieces.MineShaftCorridor.class)
public abstract class MineshaftLanternMixin extends StructurePiece {

    @Unique
    private static final BlockState lantern = ModRegistry.COPPER_LANTERN.get().defaultBlockState().setValue(LightableLanternBlock.HANGING, true);
    @Unique
    private static final BlockState torch = Blocks.WALL_TORCH.defaultBlockState();

    protected MineshaftLanternMixin(StructurePieceType p_209994_, int p_209995_, BoundingBox p_209996_) {
        super(p_209994_, p_209995_, p_209996_);
    }


    protected boolean isSupportingBox(BlockGetter p_189918_1_, BoundingBox p_189918_2_, int p_189918_3_, int p_189918_4_, int p_189918_5_, int p_189918_6_) {
        for (int i = p_189918_3_; i <= p_189918_4_; ++i) {
            if (this.getBlock(p_189918_1_, i, p_189918_5_ + 1, p_189918_6_, p_189918_2_).isAir()) {
                return false;
            }
        }
        return true;
    }

    private MineshaftStructure.Type getMineshaftType() {
        return ((MineshaftAccessor) this).getType();
    }


    //TODO: readd
    /**
     * @author mehvahdjukaar
     * @reason replacing some torches with lanterns
     */
    /*
    @Overwrite
    private void placeSupport(WorldGenLevel reader, BoundingBox boundingBox, int minX, int minY, int z, int y, int maxX, Random random) {
        if (this.isSupportingBox(reader, boundingBox, minX, maxX, y, z)) {
            BlockState plank = this.getMineshaftType().getPlanksState();
            BlockState fence = this.getMineshaftType().getFenceState();
            this.generateBox(reader, boundingBox, minX, minY, z, minX, y - 1, z, fence.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            this.generateBox(reader, boundingBox, maxX, minY, z, maxX, y - 1, z, fence.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if (random.nextInt(4) == 0) {
                this.generateBox(reader, boundingBox, minX, y, z, minX, y, z, plank, CAVE_AIR, false);
                this.generateBox(reader, boundingBox, maxX, y, z, maxX, y, z, plank, CAVE_AIR, false);
            } else {
                this.generateBox(reader, boundingBox, minX, y, z, maxX, y, z, plank, CAVE_AIR, false);

                if (RegistryConfigs.reg.HAS_MINESHAFT_LANTERN && 0.3 > random.nextFloat()) {

                    //todo: add to SHAPE_CHECK_BLOCKS. finish this

                    boolean on = random.nextFloat() > 0.2;

                    //if(!this.getBlock(reader, minX+1, y + 1, z-1, boundingBox).isAir())
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.06F, minX + 1, y, z - 1, lantern.setValue(WallLanternBlock.FACING, Direction.SOUTH)
                            .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL).setValue(LightableLanternBlock.LIT, on));
                    //if(!this.getBlock(reader, minX+1, y + 1, z-1, boundingBox).isAir())
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.06F, minX + 1, y, z + 1, lantern.setValue(WallLanternBlock.FACING, Direction.NORTH)
                            .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL).setValue(LightableLanternBlock.LIT, on));


                } else {
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.05F, minX + 1, y, z - 1, torch.setValue(WallTorchBlock.FACING, Direction.SOUTH));
                    this.maybeGenerateBlock(reader, boundingBox, random, 0.05F, minX + 1, y, z + 1, torch.setValue(WallTorchBlock.FACING, Direction.NORTH));
                }
            }

        }
    }*/


}
