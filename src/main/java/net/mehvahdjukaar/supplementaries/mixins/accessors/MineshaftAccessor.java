package net.mehvahdjukaar.supplementaries.mixins.accessors;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.gen.feature.structure.MineshaftPieces$Piece")
public interface MineshaftAccessor {

    @Invoker("getPlanksBlock")
    BlockState callGetPlanksBlock();

    @Invoker("getFenceBlock")
    BlockState callGetFenceBlock();

    @Invoker("isSupportingBox")
    boolean callIsSupportingBox(IBlockReader reader, MutableBoundingBox boundingBox, int minX, int maxX, int y, int z);
}
