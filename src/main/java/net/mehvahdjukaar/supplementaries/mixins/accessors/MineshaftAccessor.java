package net.mehvahdjukaar.supplementaries.mixins.accessors;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.gen.feature.structure.MineshaftPieces$Piece")
public interface MineshaftAccessor {

    @Invoker("getPlanksBlock")
    BlockState callGetPlanksBlock();

    @Invoker("getFenceBlock")
    BlockState callGetFenceBlock();

    @Invoker("isSupportingBox")
    boolean callIsSupportingBox(BlockGetter reader, BoundingBox boundingBox, int minX, int maxX, int y, int z);
}

