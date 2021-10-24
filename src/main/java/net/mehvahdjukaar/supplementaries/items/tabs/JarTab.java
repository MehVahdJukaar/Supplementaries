package net.mehvahdjukaar.supplementaries.items.tabs;

import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JarTab extends CreativeModeTab {

    public JarTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        ItemStack icon = new ItemStack(ModRegistry.JAR_ITEM.get());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(12);
        fluidHolder.fill(SoftFluidRegistry.HONEY);
        CompoundTag com = new CompoundTag();
        fluidHolder.save(com);
        icon.addTagElement("BlockEntityTag", com);
        return icon;
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }

    public static void tryAdd(NonNullList<ItemStack> items, CompoundTag com) {
        if (!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(ModRegistry.JAR_ITEM.get());
            returnStack.addTagElement("BlockEntityTag", com);
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }


    public static void populateTab(NonNullList<ItemStack> items) {
        JarBlockTile tempTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR_TINTED.get().defaultBlockState());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(tempTile.getMaxStackSize());


        for (Item i : CapturedMobsHelper.VALID_BUCKETS.keySet()) {
            CompoundTag com = new CompoundTag();
            MobContainer.MobData data = new MobContainer.MobData(new ItemStack(i));
            data.saveToTag(com);
            tryAdd(items, com);
        }


        for (Item i : ForgeRegistries.ITEMS) {
            ItemStack regItem = new ItemStack(i);
            CompoundTag com = new CompoundTag();
            if (tempTile.canPlaceItem(0, regItem)) {
                regItem.setCount(tempTile.getMaxStackSize());
                ContainerHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                tryAdd(items, com);
            }
        }
        for (SoftFluid s : SoftFluidRegistry.getFluids()) {
            if (s == SoftFluidRegistry.POTION || s.isEmpty()) continue;
            CompoundTag com = new CompoundTag();
            fluidHolder.clear();
            fluidHolder.fill(s);
            fluidHolder.save(com);
            tryAdd(items, com);
        }

        for (ResourceLocation potion : net.minecraft.core.Registry.POTION.keySet()) {
            CompoundTag com = new CompoundTag();
            com.putString("Potion", potion.toString());
            fluidHolder.fill(SoftFluidRegistry.POTION, com);
            CompoundTag com2 = new CompoundTag();
            fluidHolder.save(com2);
            tryAdd(items, com2);
        }


    }


}
