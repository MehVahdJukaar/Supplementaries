package net.mehvahdjukaar.supplementaries.compat.quark;

import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import vazkii.arl.util.AbstractDropIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.management.module.ShulkerBoxRightClickModule;

public class JarDropIn extends AbstractDropIn {

    private static final JarBlockTile DUMMY_JAR_TILE = new JarBlockTile();
    private static final BlockState DEFAULT_JAR = Registry.JAR.get().defaultBlockState();

    public JarDropIn() {
    }

    public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
        return ModuleLoader.INSTANCE.isModuleEnabled(ShulkerBoxRightClickModule.class) &&
                this.tryAddToShulkerBox(stack, incoming, true)!=null;
    }

    public ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
        ItemStack s = this.tryAddToShulkerBox(stack, incoming, false);
        if(s!=null)return s;
        return stack;
    }

    private ItemStack tryAddToShulkerBox(ItemStack jar, ItemStack stack, boolean simulate) {
        CompoundNBT cmp = ItemNBTHelper.getCompound(jar, "BlockEntityTag", false);
        if (cmp != null) {
            cmp = cmp.copy();
            cmp.putString("id", "supplementaries:jar");
            Item i = jar.getItem();
            if (i instanceof SackItem) {

                DUMMY_JAR_TILE.load(DEFAULT_JAR, cmp);

                ItemStack ret = DUMMY_JAR_TILE.getSoftFluidHolder().interactWithItem(stack,null,null,simulate);
                if(!simulate){
                    jar.setTag(((JarBlock)((BlockItem)jar.getItem()).getBlock()).getJarItem(DUMMY_JAR_TILE).getTag());
                }
                return ret;
            }
        }
        return null;
    }
}
