package net.mehvahdjukaar.supplementaries.mixins.accessors;


import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.MineShaftPieces$MineShaftPiece")
public interface MineshaftAccessor {

    @Accessor
    MineshaftStructure.Type getType();

}

