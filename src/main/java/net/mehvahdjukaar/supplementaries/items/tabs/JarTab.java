package net.mehvahdjukaar.supplementaries.items.tabs;

import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidHolder;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JarTab {

    public static void tryAdd(NonNullList<ItemStack> items, CompoundNBT com){
        if(!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(Registry.JAR_ITEM.get());
            returnStack.setTagInfo("BlockEntityTag", com);
            for(ItemStack i : items){
                if(i.equals(returnStack))return;
            }
            items.add(returnStack);
        }
    }


    public static void populateTab(NonNullList<ItemStack> items){
        JarBlockTile tempTile = new JarBlockTile();
        SoftFluidHolder fluidHolder = new SoftFluidHolder(tempTile.getInventoryStackLimit());
        for (Item i : ForgeRegistries.ITEMS) {
            ItemStack regItem = new ItemStack(i);
            CompoundNBT com = new CompoundNBT();
            if (tempTile.isItemValidForSlot(0, regItem)) {
                JarBlockTile.SpecialJarContent special = JarBlockTile.SpecialJarContent.get(i);
                regItem.setCount(special.isCookie()?tempTile.getInventoryStackLimit():1);
                ItemStackHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                com.putInt("SpecialType", special.ordinal());
                tryAdd(items, com);
            }
        }
        for (String id : SoftFluidList.ID_MAP.keySet()) {
            SoftFluid s = SoftFluidList.ID_MAP.get(id);
            if(s==SoftFluidList.POTION||s.isEmpty())continue;
            CompoundNBT com = new CompoundNBT();
            fluidHolder.empty();
            fluidHolder.fill(s);
            fluidHolder.write(com);
            tryAdd(items,com);
        }

        for(ResourceLocation potion : net.minecraft.util.registry.Registry.POTION.keySet()) {
            CompoundNBT com = new CompoundNBT();
            com.putString("Potion",potion.toString());
            fluidHolder.fill(SoftFluidList.POTION,com);
            CompoundNBT com2 = new CompoundNBT();
            fluidHolder.write(com2);
            tryAdd(items,com2);
        }


    }

    public static ItemStack getIcon(){
        ItemStack icon = new ItemStack(Registry.JAR_ITEM.get());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(12);
        fluidHolder.fill(SoftFluidList.HONEY);
        CompoundNBT com = new CompoundNBT();
        fluidHolder.write(com);
        icon.setTagInfo("BlockEntityTag", com);
        return icon;
    }
}
