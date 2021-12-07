package net.mehvahdjukaar.supplementaries.compat.cctweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.supplementaries.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class CCStuff {

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }

    public static void initialize() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }



    public static SpeakerBlock makeSpeaker() {
        //try loading this damn classloader
        class SpeakerCC extends SpeakerBlock implements IPeripheralProvider {

            @NotNull
            @Override
            public LazyOptional<IPeripheral> getPeripheral(@NotNull World world, @NotNull BlockPos pos, @NotNull Direction side) {
                TileEntity tile = world.getBlockEntity(pos);
                if (tile instanceof SpeakerBlockTile) {
                    return ((SpeakerBlockTile) tile).getPeripheral(world, pos, side).cast();
                }
                return LazyOptional.empty();
            }
        }
        return new SpeakerCC();
    }
}
