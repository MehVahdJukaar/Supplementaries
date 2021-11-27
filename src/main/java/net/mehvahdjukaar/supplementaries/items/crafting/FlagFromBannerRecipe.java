package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FlagFromBannerRecipe extends SpecialRecipe {
    public FlagFromBannerRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    public boolean matches(CraftingInventory inv, World world) {
        DyeColor dyecolor = null;
        ItemStack withPatterns = null;
        ItemStack empty = null;

        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemstack2 = inv.getItem(i);
            Item item = itemstack2.getItem();
            if (item instanceof FlagItem) {
                FlagItem banneritem = (FlagItem)item;
                if (dyecolor == null) {
                    dyecolor = banneritem.getColor();
                } else if (dyecolor != banneritem.getColor()) {
                    return false;
                }

                int j = BannerTileEntity.getPatternCount(itemstack2);
                if (j > 6) {
                    return false;
                }

                if (j > 0) {
                    if (withPatterns != null) {
                        return false;
                    }

                    withPatterns = itemstack2;
                }
                else {
                    if (empty != null) {
                        return false;
                    }

                    empty = itemstack2;
                }

            }
            if (item instanceof BannerItem) {
                BannerItem banneritem = (BannerItem)item;
                if (dyecolor == null) {
                    dyecolor = banneritem.getColor();
                } else if (dyecolor != banneritem.getColor()) {
                    return false;
                }

                int j = BannerTileEntity.getPatternCount(itemstack2);
                if (j > 6) {
                    return false;
                }
                //exclude banner to banner
                if (j > 0 && !(empty!=null && empty.getItem() instanceof BannerItem)) {
                    if (withPatterns != null) {
                        return false;
                    }
                    withPatterns = itemstack2;
                }
                else if(!(withPatterns!=null && withPatterns.getItem() instanceof BannerItem)){
                    if (empty != null) {
                        return false;
                    }

                    empty = itemstack2;
                }
            }
        }

        return withPatterns != null && empty != null;
    }

    public ItemStack assemble(CraftingInventory inv) {
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack withPatterns = inv.getItem(i);
            if (!withPatterns.isEmpty()) {
                int j = BannerTileEntity.getPatternCount(withPatterns);
                //find item with patterns
                if (j > 0 && j <= 6) {
                    for(int k = 0; k < inv.getContainerSize(); ++k) {
                        if(i!=k) {
                            ItemStack empty = inv.getItem(k);


                            //find other which must be empty. exclude banner to banner
                            Item it = empty.getItem();
                            if (it instanceof FlagItem || it instanceof BannerItem) {
                                ItemStack result = empty.copy();
                                result.setCount(1);
                                result.setTag(withPatterns.getTag());
                                return result;
                            }
                        }
                    }

                }
            }
        }

        return ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.hasContainerItem()) {
                    nonnulllist.set(i, itemstack.getContainerItem());
                } else if (itemstack.hasTag() && BannerTileEntity.getPatternCount(itemstack) > 0) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(1);
                    nonnulllist.set(i, itemstack1);
                }
            }
        }

        return nonnulllist;
    }

    public IRecipeSerializer<?> getSerializer() {
        return IRecipeSerializer.BANNER_DUPLICATE;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}