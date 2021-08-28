package net.mehvahdjukaar.supplementaries.items.tabs;

import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JarTab {

    public static void tryAdd(NonNullList<ItemStack> items, CompoundNBT com) {
        if (!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(Registry.JAR_ITEM.get());
            returnStack.addTagElement("BlockEntityTag", com);
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }


    public static void populateTab(NonNullList<ItemStack> items) {
        JarBlockTile tempTile = new JarBlockTile();
        SoftFluidHolder fluidHolder = new SoftFluidHolder(tempTile.getMaxStackSize());


        for (Item i : CapturedMobsHelper.VALID_BUCKETS.keySet()) {
            CompoundNBT com = new CompoundNBT();
            MobHolder.saveBucketToNBT(com, new ItemStack(i), CapturedMobsHelper.getDefaultNameFromBucket(i),
                    CapturedMobsHelper.getTypeFromBucket(i).getFishTexture());
            tryAdd(items, com);
        }


        for (Item i : ForgeRegistries.ITEMS) {
            ItemStack regItem = new ItemStack(i);
            CompoundNBT com = new CompoundNBT();
            if (tempTile.canPlaceItem(0, regItem)) {
                regItem.setCount(tempTile.getMaxStackSize());
                ItemStackHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                tryAdd(items, com);
            }
        }
        for (SoftFluid s : SoftFluidRegistry.getFluids()) {
            if (s == SoftFluidRegistry.POTION || s.isEmpty()) continue;
            CompoundNBT com = new CompoundNBT();
            fluidHolder.clear();
            fluidHolder.fill(s);
            fluidHolder.save(com);
            tryAdd(items, com);
        }

        for (ResourceLocation potion : net.minecraft.util.registry.Registry.POTION.keySet()) {
            CompoundNBT com = new CompoundNBT();
            com.putString("Potion", potion.toString());
            fluidHolder.fill(SoftFluidRegistry.POTION, com);
            CompoundNBT com2 = new CompoundNBT();
            fluidHolder.save(com2);
            tryAdd(items, com2);
        }


    }

    public static ItemStack getIcon() {
        ItemStack icon = new ItemStack(Registry.JAR_ITEM.get());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(12);
        fluidHolder.fill(SoftFluidRegistry.HONEY);
        CompoundNBT com = new CompoundNBT();
        fluidHolder.save(com);
        icon.addTagElement("BlockEntityTag", com);
        return icon;
    }
}
