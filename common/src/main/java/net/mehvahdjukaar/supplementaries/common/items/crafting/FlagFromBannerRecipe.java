package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class FlagFromBannerRecipe extends CustomRecipe {
    public FlagFromBannerRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        DyeColor dyecolor = null;
        ItemStack withPatterns = null;
        ItemStack empty = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof FlagItem flagItem) {
                if (dyecolor == null) {
                    dyecolor = flagItem.getColor();
                } else if (dyecolor != flagItem.getColor()) {
                    return false;
                }

                int j = BannerBlockEntity.getPatternCount(itemStack);
                if (j > 6) {
                    return false;
                }

                if (j > 0) {
                    if (withPatterns != null) {
                        return false;
                    }

                    withPatterns = itemStack;
                } else {
                    if (empty != null) {
                        return false;
                    }

                    empty = itemStack;
                }

            }
            else if (item instanceof BannerItem banneritem) {
                if (dyecolor == null) {
                    dyecolor = banneritem.getColor();
                } else if (dyecolor != banneritem.getColor()) {
                    return false;
                }

                int j = BannerBlockEntity.getPatternCount(itemStack);
                if (j > 6) {
                    return false;
                }
                //exclude banner to banner
                if (j > 0 && !(empty != null && empty.getItem() instanceof BannerItem)) {
                    if (withPatterns != null) {
                        return false;
                    }
                    withPatterns = itemStack;
                } else if (!(withPatterns != null && withPatterns.getItem() instanceof BannerItem)) {
                    if (empty != null) {
                        return false;
                    }

                    empty = itemStack;
                }
            }
            else if(!itemStack.isEmpty())return false;
        }

        return withPatterns != null && empty != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack withPatterns = inv.getItem(i);
            if (!withPatterns.isEmpty()) {
                int patternCount = BannerBlockEntity.getPatternCount(withPatterns);
                //find item with patterns
                if (patternCount > 0 && patternCount <= 6) {
                    for (int k = 0; k < inv.getContainerSize(); ++k) {
                        if (i != k) {
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

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.hasContainerItem()) {
                    stacks.set(i, itemstack.getContainerItem());
                } else if (itemstack.hasTag() && BannerBlockEntity.getPatternCount(itemstack) > 0) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(1);
                    stacks.set(i, itemstack1);
                }
            }
        }

        return stacks;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.FLAG_FROM_BANNER_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}