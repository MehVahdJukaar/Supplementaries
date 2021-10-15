package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class FlagFromBannerRecipe extends CustomRecipe {
    public FlagFromBannerRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    public boolean matches(CraftingContainer inv, Level world) {
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

                int j = BannerBlockEntity.getPatternCount(itemstack2);
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

                int j = BannerBlockEntity.getPatternCount(itemstack2);
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

    public ItemStack assemble(CraftingContainer inv) {
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack withPatterns = inv.getItem(i);
            if (!withPatterns.isEmpty()) {
                int j = BannerBlockEntity.getPatternCount(withPatterns);
                //find item with patterns
                if (j > 0 && j <= 6) {
                    for(int k = 0; k < inv.getContainerSize(); ++k) {
                        if(i!=j) {
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

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.hasContainerItem()) {
                    nonnulllist.set(i, itemstack.getContainerItem());
                } else if (itemstack.hasTag() && BannerBlockEntity.getPatternCount(itemstack) > 0) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(1);
                    nonnulllist.set(i, itemstack1);
                }
            }
        }

        return nonnulllist;
    }

    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}