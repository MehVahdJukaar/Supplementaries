package net.mehvahdjukaar.supplementaries.world.data.map.lib;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CustomDecorationType<D extends CustomDecoration, M extends MapWorldMarker<D>> {
    private final ResourceLocation id;
    private final Function<CompoundNBT,M> loadMarker;
    private final BiFunction<IBlockReader,BlockPos,M> fromWorld;
    private final BiFunction<CustomDecorationType<?,?>,PacketBuffer,D> decoFromBuffer;
    private final boolean hasMarker;
    public CustomDecorationType(ResourceLocation id,Function<CompoundNBT,M> loadMarkerFromNBT, BiFunction<IBlockReader,BlockPos,M>getWorldMarkerFromWorld,
                                BiFunction<CustomDecorationType<?,?>,PacketBuffer,D> decoFromBuffer){
        this.id = id;
        this.loadMarker = loadMarkerFromNBT;
        this.fromWorld = getWorldMarkerFromWorld;
        this.decoFromBuffer = decoFromBuffer;
        this.hasMarker = true;
    }
    public CustomDecorationType(ResourceLocation id,BiFunction<CustomDecorationType<?,?>,PacketBuffer,D> decoFromBuffer){
        this.id = id;
        this.loadMarker = s->null;
        this.fromWorld = (s,d)->null;
        this.decoFromBuffer = decoFromBuffer;
        this.hasMarker = false;
    }


    public boolean hasMarker() {
        return hasMarker;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getSerializeId() {
        return id.toString();
    }

    @Nullable
    public M loadWorldMarkerFromNBT(CompoundNBT compound){
        return hasMarker ? loadMarker.apply(compound) : null;
    }

    @Nullable
    public M getWorldMarkerFromWorld(IBlockReader reader, BlockPos pos){
        return hasMarker ? fromWorld.apply(reader,pos) : null;
    }

    @Nullable
    public D loadDecorationFromBuffer(PacketBuffer buffer){
        return decoFromBuffer.apply(this,buffer);
    }
}
