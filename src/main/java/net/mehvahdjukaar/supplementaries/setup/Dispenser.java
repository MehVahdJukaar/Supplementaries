package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.FireflyJarBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.items.EmptyJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class Dispenser {

    //this is a copy of vanilla dispenser behaviors
    static public Map<Item, IDispenseItemBehavior> DEFAULT_BEHAVIORS;
    //TODO: find a better way to do this

    public static void registerBehaviors() {

        if(!RegistryConfigs.reg.DISPENSERS.get())return;

        DEFAULT_BEHAVIORS = Dispenser.getVanillaDispenserBehaviors();
        if(DEFAULT_BEHAVIORS==null){
            Supplementaries.LOGGER.info("Failed to register dispenser behaviors");
            return;
        }
        //jar
        if(RegistryConfigs.reg.JAR_ENABLED.get()){
            for(Item item : ForgeRegistries.ITEMS) {
                if (item instanceof JarItem || item instanceof EmptyJarItem || item instanceof SackItem ||
                        (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof FireflyJarBlock)) {
                    DispenserBlock.registerDispenseBehavior(item, new PlaceBlockDispenseBehavior());
                }
                else if (!CommonUtil.getJarContentTypeFromItem(new ItemStack(item)).isEmpty()) {
                    DispenserBlock.registerDispenseBehavior(item, new FillJarDispenserBehavior());
                }
            }
            DispenserBlock.registerDispenseBehavior(Items.BUCKET, new BucketJarDispenserBehavior());
            DispenserBlock.registerDispenseBehavior(Items.BOWL, new BucketJarDispenserBehavior());
            DispenserBlock.registerDispenseBehavior(Items.GLASS_BOTTLE, new BucketJarDispenserBehavior());
        }

        DispenserBlock.registerDispenseBehavior(Items.FLINT_AND_STEEL, new FlintAndSteelDispenserBehavior());
        DispenserBlock.registerDispenseBehavior(Items.LINGERING_POTION, new BambooSpikesDispenserBehavior());

        if(ServerConfigs.cached.THROWABLE_BRICKS_ENABLED){
            DispenserBlock.registerDispenseBehavior(Items.NETHER_BRICK, new ThrowableBricksDispenserBehavior(ModTags.BRICKS));
            DispenserBlock.registerDispenseBehavior(Items.BRICK, new ThrowableBricksDispenserBehavior(ModTags.BRICKS));
        }

        //firefly
        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            DispenserBlock.registerDispenseBehavior(Registry.FIREFLY_SPAWN_EGG_ITEM.get(), spawneggBehavior);
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
    private static final DefaultDispenseItemBehavior shootBehavior = new DefaultDispenseItemBehavior();

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

    //returns full bottle to dispenser. same function that's in IDispenseritemBehavior
    public static ItemStack glassBottleFill(IBlockSource source, ItemStack empty, ItemStack filled) {
        empty.shrink(1);
        if (empty.isEmpty()) {
            return filled.copy();
        } else {
            if (!Dispenser.MergeDispenserItem(source.getBlockTileEntity(), filled)) {
                Dispenser.shootBehavior.dispense(source, filled.copy());
            }
            return empty;
        }
    }

    public static abstract class TaggedAdditionalDispenserBehavior extends AdditionalDispenserBehavior {
        private final ITag<Item> tag;
        TaggedAdditionalDispenserBehavior(ITag<Item> tag){
            this.tag = tag;
        }
        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            if(ModTags.isTagged(tag,stack.getItem())){
                return super.dispenseStack(source,stack);
            }
            //vanilla behavior
            return super.customBehavior(source,stack);
        }

        @Override
        protected abstract ItemStack customBehavior(IBlockSource source, ItemStack stack);
    }

    //TODO: there must be an easier and cleaner way
    public static abstract class AdditionalDispenserBehavior extends DefaultDispenseItemBehavior {
        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            try{
                return this.customBehavior(source,stack);
            }
            catch (Exception e) {
                //failsafe shoot behavior (hopefully)
                return shootBehavior.dispense(source, stack);
            }
        }
        //default vanilla behavior
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack){
            return Dispenser.DEFAULT_BEHAVIORS.get(stack.getItem()).dispense(source,stack);
        }
    }

    public static class FillJarDispenserBehavior extends AdditionalDispenserBehavior {

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.mobHolder.isEmpty()) {
                    ItemStack returnStack = ItemStack.EMPTY;
                    if (tile.isItemValidForSlot(0, stack)) {
                        tile.handleAddItem(stack, null, null);
                        tile.markDirty();
                        //this.setSuccessful(true);
                        returnStack = new ItemStack(stack.getItem() instanceof FishBucketItem ? Items.BUCKET : Items.AIR);
                    } else if (tile.isEmpty()) {
                        returnStack = tile.fluidHolder.interactWithItem(stack);
                        if(!returnStack.isEmpty())tile.markDirty();
                    }
                    return Dispenser.glassBottleFill(source, stack, returnStack);
                }
                return stack;
            }
            return super.customBehavior(source,stack);
        }
    }


    public static class FlintAndSteelDispenserBehavior extends AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if(state.getBlock() instanceof LightUpBlock){
                if(LightUpBlock.lightUp(state,blockpos,world,LightUpBlock.FireSound.FLINT_AND_STEEL)){
                    if(stack.attemptDamageItem(1, world.rand, (ServerPlayerEntity)null)){
                        stack.setCount(0);
                    }
                }
                return stack;
            }
            return super.customBehavior(source,stack);
        }
    }


    public static class ThrowableBricksDispenserBehavior extends TaggedAdditionalDispenserBehavior{

        ThrowableBricksDispenserBehavior(ITag<Item> tag) {
            super(tag);
        }

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            World world = source.getWorld();
            IPosition iposition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            ProjectileEntity projectileentity = this.getProjectileEntity(world, iposition, stack);
            projectileentity.shoot(direction.getXOffset(), (float)direction.getYOffset() + 0.1F, direction.getZOffset(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addEntity(projectileentity);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playDispenseSound(IBlockSource source) {
            source.getWorld().playSound(null, source.getX()+0.5, source.getY()+0.5, source.getZ()+0.5, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (source.getWorld().getRandom().nextFloat() * 0.4F + 0.8F ));
        }

        protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn){
            ThrowableBrickEntity brickEntity = new ThrowableBrickEntity(worldIn, position.getX(), position.getY(), position.getZ());
            return brickEntity;
        }

        protected float getProjectileInaccuracy() {
            return 7.0F;
        }
        //TODO: fix throwable bricks rendering glitchyness
        protected float getProjectileVelocity() {
            return 0.9F;
        }

    }

    public static class BambooSpikesDispenserBehavior extends AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if(state.getBlock() instanceof BambooSpikesBlock){
                if(BambooSpikesBlock.tryAddingPotion(state,world,blockpos,stack)){
                    return Dispenser.glassBottleFill(source, stack, new ItemStack(Items.GLASS_BOTTLE));
                }
                return stack;
            }

            return super.customBehavior(source,stack);
        }
    }


    public static class BucketJarDispenserBehavior extends  AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            TileEntity te = world.getTileEntity(blockpos);
            if(te instanceof JarBlockTile){
                //TODO: add fish buckets
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.mobHolder.isEmpty() && tile.isEmpty()) {
                    ItemStack returnStack = tile.fluidHolder.interactWithItem(stack);
                    if(!returnStack.isEmpty()){
                        tile.markDirty();
                        return Dispenser.glassBottleFill(source, stack,returnStack);
                    }
                }
                return stack;
            }

            return super.customBehavior(source,stack);
        }

    }

    public static class PlaceBlockDispenseBehavior extends OptionalDispenseBehavior {

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