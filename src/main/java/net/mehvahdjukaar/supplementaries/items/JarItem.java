package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.client.renderers.PotionTooltipHelper;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.mehvahdjukaar.supplementaries.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class JarItem extends CageItem {
    public JarItem(Block blockIn, Properties properties, Supplier<Item> empty) {
        super(blockIn, properties,empty);
    }

    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").mergeStyle(TextFormatting.GRAY));
            }

            if(compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidList.fromID(com.getString("Fluid"));
                int count = com.getInt("Count");
                if (!s.isEmpty() && count > 0) {
                    tooltip.add(new TranslationTextComponent("message.supplementaries.fluid_tooltip",
                            s.getTranslatedName(), count).mergeStyle(TextFormatting.GRAY));


                    if (com.contains("NBT") && com.getCompound("NBT").contains("Potion")) {
                        PotionTooltipHelper.addPotionTooltip(com.getCompound("NBT"), tooltip, 1);
                        return;
                    }
                }
            }

            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for(ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getDisplayName().deepCopy();

                            String s = iformattabletextcomponent.getString();
                            s = s.replace(" Bucket", "");
                            s = s.replace(" Bottle", "");
                            s = s.replace("Bucket of ", "");
                            IFormattableTextComponent str = new StringTextComponent(s);

                            str.appendString(" x").appendString(String.valueOf(itemstack.getCount()));
                            tooltip.add(str.mergeStyle(TextFormatting.GRAY));
                        }
                    }
                }
                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group) && RegistryConfigs.reg.JAR_TAB.get() && group == Registry.JAR_TAB) {
            JarTab.populateTab(items);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidList.fromID(com.getString("Fluid"));
                if(s==SoftFluidList.DIRT)return Rarity.RARE;
            }
        }
        return super.getRarity(stack);
    }
}
