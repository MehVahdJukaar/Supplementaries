package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.blocks.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.items.EmptyJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class Dispenser {



    static public Map<Item, IDispenseItemBehavior> DEFAULT_BEHAVIORS;


    public static void registerBehaviors() {

        DEFAULT_BEHAVIORS = Dispenser.getVanillaDispenserBehaviors();
        if(DEFAULT_BEHAVIORS==null) return;

        for(Item item : ForgeRegistries.ITEMS) {
            if(item instanceof JarItem || item instanceof EmptyJarItem){
                DispenserBlock.registerDispenseBehavior(item, new JarItemDispenseBehavior());
            }
            else if(!CommonUtil.getJarContentTypeFromItem(new ItemStack(item)).isEmpty()){
                DispenserBlock.registerDispenseBehavior(item, new FillJarDispenserBehavior());
            }
            else if(item==Items.BUCKET){
                DispenserBlock.registerDispenseBehavior(item, new BucketJarDispenserBehavior());
            }
            else if(item==Items.BOWL){
                DispenserBlock.registerDispenseBehavior(item, new BowlJarDispenserBehavior());
            }
            else if(item==Items.GLASS_BOTTLE){
                DispenserBlock.registerDispenseBehavior(item, new BottleJarDispenserBehavior());
            }
            else if (item == Registry.FIREFLY_SPAWN_EGG_ITEM){
                DispenserBlock.registerDispenseBehavior(item, spawneggBehavior);
            }
        }
    }

    private static final DefaultDispenseItemBehavior spawneggBehavior = new DefaultDispenseItemBehavior() {
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            EntityType<?> entitytype = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            entitytype.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
            stack.shrink(1);
            return stack;
        }
    };
    private static final DefaultDispenseItemBehavior defaultBehaviour = new DefaultDispenseItemBehavior();

    private static Map<Item, IDispenseItemBehavior> getVanillaDispenserBehaviors(){
        try {
            Field f = ObfuscationReflectionHelper.findField(DispenserBlock.class,"field_149943_a");
            f.setAccessible(true);
            Map<Item,IDispenseItemBehavior> m = ((Map<Item, IDispenseItemBehavior>) f.get(null));
            HashMap<Item, IDispenseItemBehavior> map = new HashMap<>();
            for (Item i:m.keySet()) {
                map.put(i, m.get(i));
            }
            return map;
        }
        catch (Exception ignored) {
            return null;
        }
    }


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
                //Dispenser.defaultBehaviour.dispense(source, filled.copy());
            }
            return empty;
        }
    }

    //TODO: there must be an easier and cleaner way
    public static abstract class AdditionalDispenserBehavior extends DefaultDispenseItemBehavior {
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            try{
                return this.customBehavior(source,stack);
            }
            catch (Exception e) {
                return defaultBehaviour.dispense(source, stack);
            }
        }
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack){
            return Dispenser.DEFAULT_BEHAVIORS.get(stack.getItem()).dispense(source,stack);
        }
    }

    public static class FillJarDispenserBehavior extends AdditionalDispenserBehavior {

        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.isItemValidForSlot(0, stack)){
                    tile.handleAddItem(stack, null, null);
                    tile.markDirty();
                    //this.setSuccessful(true);
                    return Dispenser.glassBottleFill(source, stack, new ItemStack(
                            stack.getItem() == Items.WATER_BUCKET? Items.BUCKET : tile.liquidType.getReturnItem()));
                }
            }
            return super.customBehavior(source,stack);
        }
    }


    public static class BucketJarDispenserBehavior extends  AdditionalDispenserBehavior{

        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if (tile.liquidType.bucket) {
                    // if extraction successful
                    ItemStack returnitem  = tile.extractItem(3);
                    if(returnitem!=null){
                        tile.markDirty();
                        //this.setSuccessful(true);
                        return Dispenser.glassBottleFill(source,stack,returnitem);
                    }
                }
            }

            return super.customBehavior(source,stack);
        }

    }





    public static class BottleJarDispenserBehavior extends  AdditionalDispenserBehavior{

        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if (tile.liquidType.bottle) {
                    // if extraction successful
                    ItemStack returnitem  = tile.extractItem(1);
                    if(returnitem!=null){
                        tile.markDirty();
                        //this.setSuccessful(true);
                        return Dispenser.glassBottleFill(source,stack,returnitem);
                    }

                }
            }
            return super.customBehavior(source,stack);
        }

    }

    public static class BowlJarDispenserBehavior extends  AdditionalDispenserBehavior{

        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if (tile.liquidType.bowl) {
                    // if extraction successful
                    ItemStack returnitem  = tile.extractItem(2);
                    if(returnitem!=null){
                        tile.markDirty();
                        //this.setSuccessful(true);
                        return Dispenser.glassBottleFill(source,stack,returnitem);
                    }

                }
            }
            return super.customBehavior(source, stack);
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
                this.setSuccessful(result.isSuccessOrConsume());
            }
            return stack;
        }
    }
}