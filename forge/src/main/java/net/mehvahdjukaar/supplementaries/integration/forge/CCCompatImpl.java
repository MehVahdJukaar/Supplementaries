package net.mehvahdjukaar.supplementaries.integration.forge;


import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;


public class CCCompatImpl {
    public static <T> boolean isPeripheralCap(Capability<T> cap) {
        return false;
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile speakerBlockTile) {
        return null;

    }

    public static void initialize() {
    }

    public static boolean isPrintedBook(Item item) {
        return false;
    }

    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties p) {
        return null;
    }

    public static int getPages(ItemStack itemstack) {
        return 0;
    }

    public static String[] getText(ItemStack itemstack) {
        return null;
    }
/*

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.Capabilities;
import dan200.computercraft.shared.media.items.ItemPrintout;

    public static void initialize() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }

    public static int getPages(ItemStack itemstack) {
        return ItemPrintout.getPageCount(itemstack);
    }

    public static String[] getText(ItemStack itemstack) {
        return ItemPrintout.getText(itemstack);
    }

    public static boolean isPrintedBook(Item item) {
        return item instanceof ItemPrintout;
    }

    //TODO: maybe this isn't needed since tile alredy provides it
    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties properties) {
        //try loading this now, freaking classloader
        class SpeakerCC extends SpeakerBlock implements IPeripheralProvider {

            public SpeakerCC(Properties properties) {
                super(properties);
            }

            @NotNull
            @Override
            public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
                var tile = world.getBlockEntity(pos);
                if (tile != null) {
                    return tile.getCapability(Capabilities.CAPABILITY_PERIPHERAL, side);
                }
                return LazyOptional.empty();
            }
        }
        return new SpeakerCC(properties);
    }

    public static boolean isPeripheralCap(Capability<?> cap) {
        return cap == Capabilities.CAPABILITY_PERIPHERAL;
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }
*/
}
