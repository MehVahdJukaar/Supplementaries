package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.selene.util.PotionNBTHelper;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.fluids.ModSoftFluids;
import net.mehvahdjukaar.supplementaries.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
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

    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").withStyle(TextFormatting.GRAY));
            }

            if(compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidRegistry.get(com.getString("Fluid"));
                int count = com.getInt("Count");
                if (!s.isEmpty() && count > 0) {

                    CompoundNBT nbt = null;
                    String add = "";
                    if (com.contains("NBT")){
                        nbt = com.getCompound("NBT");
                        if(nbt.contains("Bottle")){
                            String bottle = nbt.getString("Bottle").toLowerCase();
                            if(!bottle.equals("bottle")) add = "_"+bottle;
                        }
                    }

                    tooltip.add(new TranslationTextComponent("message.supplementaries.fluid_tooltip",
                            new TranslationTextComponent(s.getTranslationKey()+add), count).withStyle(TextFormatting.GRAY));
                    if(nbt != null) {
                        PotionNBTHelper.addPotionTooltip(nbt, tooltip, 1);
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
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getHoverName().copy();

                            String s = iformattabletextcomponent.getString();
                            s = s.replace(" Bucket", "");
                            s = s.replace(" Bottle", "");
                            s = s.replace("Bucket of ", "");
                            IFormattableTextComponent str = new StringTextComponent(s);

                            str.append(" x").append(String.valueOf(itemstack.getCount()));
                            tooltip.add(str.withStyle(TextFormatting.GRAY));
                        }
                    }
                }
                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group) && RegistryConfigs.reg.JAR_TAB.get() && group == Registry.JAR_TAB) {
            JarTab.populateTab(items);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidRegistry.get(com.getString("Fluid"));
                if(s== ModSoftFluids.DIRT)return Rarity.RARE;
            }
        }
        return super.getRarity(stack);
    }
}
