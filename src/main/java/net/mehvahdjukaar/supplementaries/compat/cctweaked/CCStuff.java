package net.mehvahdjukaar.supplementaries.compat.cctweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.mehvahdjukaar.supplementaries.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class CCStuff {

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }

    public static void initialize() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }


    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties properties) {
        //try loading this now, freaking classloader
        class SpeakerCC extends SpeakerBlock implements IPeripheralProvider {

            public SpeakerCC(Properties properties) {
                super(properties);
            }

            @NotNull
            @Override
            public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
                if (world.getBlockEntity(pos) instanceof SpeakerBlockTile tile) {
                    return tile.getPeripheral(world, pos, side).cast();
                }
                return LazyOptional.empty();
            }
        }
        return new SpeakerCC(properties);
    }

    public static boolean checkForPrintedBook(Item item) {
        return item instanceof ItemPrintout;
    }

    public static int getPages(ItemStack itemstack) {
        return ItemPrintout.getPageCount(itemstack);
    }

    public static String[] getText(ItemStack itemstack) {
        return ItemPrintout.getText(itemstack);
    }
}
