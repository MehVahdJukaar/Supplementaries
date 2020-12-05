package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.blocks.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.items.EmptyJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.item.*;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;


public class Dispenser {

    public static void registerBehaviors() {
        for(Item item : ForgeRegistries.ITEMS) {
            if(item instanceof JarItem || item instanceof EmptyJarItem){
                DispenserBlock.registerDispenseBehavior(item, new JarItemDispenseBehavior());
            }
            else if(!CommonUtil.getJarContentTypeFromItem(new ItemStack(item)).isEmpty()){
                DispenserBlock.registerDispenseBehavior(item, new FillJarDispenserBehavior());
            }
        }
    }

    private static final DefaultDispenseItemBehavior defaultBehaviour = new DefaultDispenseItemBehavior();

    //add item to dispenser and merges it if there's one already
    public static boolean MergeDispenserItem(DispenserTileEntity te, ItemStack filled) {
        try {
            //field_174913_f, field_146022_i
            Field f = ObfuscationReflectionHelper.findField(DispenserTileEntity.class,"field_146022_i");
            f.setAccessible(true);
            NonNullList<ItemStack> stacks = (NonNullList<ItemStack>) f.get(te);
            for (int i = 0; i < te.getSizeInventory(); ++i) {
                ItemStack s = stacks.get(i);
                if (s.isEmpty() || (s.getItem() == filled.getItem() && s.getMaxStackSize()>s.getCount())) {
                    filled.grow(s.getCount());
                    te.setInventorySlotContents(i, filled);
                    return true;
                }
            }
        } catch (Exception ignored) {
            return te.addItemStack(filled.copy()) < 0;
        }
        return false;
    }

    //returns full bottle to dispenser
    public static ItemStack glassBottleFill(IBlockSource source, ItemStack empty, ItemStack filled) {
        empty.shrink(1);
        if (empty.isEmpty()) {
            return filled.copy();
        } else {
            if (!Dispenser.MergeDispenserItem(source.getBlockTileEntity(), filled)) {
                Dispenser.defaultBehaviour.dispense(source, filled.copy());
            }
            return empty;
        }
    }


    public static class FillJarDispenserBehavior extends OptionalDispenseBehavior {

        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.isItemValidForSlot(0, stack)){
                    tile.handleAddItem(stack, null, null);
                    tile.markDirty();
                    this.setSuccessful(true);

                    return Dispenser.glassBottleFill(source, stack, new ItemStack(
                            stack.getItem() == Items.WATER_BUCKET? Items.BUCKET : tile.liquidType.getReturnItem()));
                }
            }
            return stack;
        }
    }

    //TODO: finish this
    public static class BucketJarDispenserBehavior extends  OptionalDispenseBehavior{

        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if (tile.liquidType.bottle) {
                    // if extraction successful
                    boolean success = tile.extractItem(1, stack, null, null);

                    }
                }

            return stack;
        }

    }

    public static class BottleJarDispenserBehavior extends  OptionalDispenseBehavior{

        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);

            return stack;
        }

    }

    public static class BowlJarDispenserBehavior extends  OptionalDispenseBehavior{

        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);

            return stack;
        }

    }

    public static class JarItemDispenseBehavior extends OptionalDispenseBehavior {

        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockpos = source.getBlockPos().offset(direction);
                Direction direction1 = source.getWorld().isAirBlock(blockpos.down()) ? direction : Direction.UP;
                ActionResultType result = ((BlockItem)item).tryPlace(new DirectionalPlaceContext(source.getWorld(), blockpos, direction, stack, direction1));
                this.setSuccessful(result== ActionResultType.SUCCESS);
            }
            return stack;
        }
    }
}