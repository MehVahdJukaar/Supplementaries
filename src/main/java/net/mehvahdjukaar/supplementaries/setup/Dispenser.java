package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.FireflyJarBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobs;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;


public class Dispenser {

    //this is a copy of vanilla dispenser behaviors
    static public Map<Item, IDispenseItemBehavior> DEFAULT_BEHAVIORS;
    //TODO: find a better way to do this

    public static void registerBehaviors() {

        if(!RegistryConfigs.reg.DISPENSERS.get())return;

        DEFAULT_BEHAVIORS = new HashMap<>(DispenserBlock.DISPENSER_REGISTRY);
        if(DEFAULT_BEHAVIORS==null){
            Supplementaries.LOGGER.info("Failed to register dispenser behaviors");
            return;
        }
        //jar
        if(RegistryConfigs.reg.JAR_ENABLED.get()){
            for(Item item : ForgeRegistries.ITEMS) {
                if (item instanceof JarItem || item instanceof EmptyJarItem || item instanceof SackItem ||
                        (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof FireflyJarBlock)) {
                    DispenserBlock.registerBehavior(item, new PlaceBlockDispenseBehavior());
                }
                else if(item instanceof FishBucketItem || item==Items.COOKIE || item==Items.WATER_BUCKET
                        || SoftFluidList.ITEM_MAP.containsKey(item))
                    DispenserBlock.registerBehavior(item, new FillJarDispenserBehavior());

            }
            DispenserBlock.registerBehavior(Items.BUCKET, new BucketJarDispenserBehavior());
            DispenserBlock.registerBehavior(Items.BOWL, new BucketJarDispenserBehavior());
            DispenserBlock.registerBehavior(Items.GLASS_BOTTLE, new BucketJarDispenserBehavior());

            for(Item i : CapturedMobs.VALID_BUCKETS.keySet()){
                DispenserBlock.registerBehavior(i, new FishBucketJarDispenserBehavior());
            }
        }

        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new FlintAndSteelDispenserBehavior());
        DispenserBlock.registerBehavior(Items.LINGERING_POTION, new BambooSpikesDispenserBehavior());

        if(ServerConfigs.cached.THROWABLE_BRICKS_ENABLED){
            DispenserBlock.registerBehavior(Items.NETHER_BRICK, new ThrowableBricksDispenserBehavior(ModTags.BRICKS));
            DispenserBlock.registerBehavior(Items.BRICK, new ThrowableBricksDispenserBehavior(ModTags.BRICKS));
        }

        //firefly
        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            DispenserBlock.registerBehavior(Registry.FIREFLY_SPAWN_EGG_ITEM.get(), spawneggBehavior);
        }
        if(RegistryConfigs.reg.BOMB_ENABLED.get()){
            DispenserBlock.registerBehavior(Registry.BOMB_ITEM.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_BLUE_ITEM.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_ITEM_ON.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_BLUE_ITEM_ON.get(), new BombsDispenserBehavior());
        }

    }

    private static final DefaultDispenseItemBehavior spawneggBehavior = new DefaultDispenseItemBehavior() {
        public ItemStack execute(IBlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> entitytype = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            entitytype.spawn(source.getLevel(), stack, null, source.getPos().relative(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
            stack.shrink(1);
            return stack;
        }
    };
    private static final DefaultDispenseItemBehavior shootBehavior = new DefaultDispenseItemBehavior();


    //add item to dispenser and merges it if there's one already
    private static boolean MergeDispenserItem(DispenserTileEntity te, ItemStack filled) {
        NonNullList<ItemStack> stacks = te.items;
        for (int i = 0; i < te.getContainerSize(); ++i) {
            ItemStack s = stacks.get(i);
            if (s.isEmpty() || (s.getItem() == filled.getItem() && s.getMaxStackSize()>s.getCount())) {
                filled.grow(s.getCount());
                te.setItem(i, filled);
                return true;
            }
        }
        return false;
    }

    //returns full bottle to dispenser. same function that's in IDispenseritemBehavior
    private static ItemStack glassBottleFill(IBlockSource source, ItemStack empty, ItemStack filled) {
        empty.shrink(1);
        if (empty.isEmpty()) {
            return filled.copy();
        } else {
            if (!Dispenser.MergeDispenserItem(source.getEntity(), filled)) {
                Dispenser.shootBehavior.dispense(source, filled.copy());
            }
            return empty;
        }
    }

    private static abstract class TaggedAdditionalDispenserBehavior extends AdditionalDispenserBehavior {
        private final ITag<Item> tag;
        TaggedAdditionalDispenserBehavior(ITag<Item> tag){
            this.tag = tag;
        }
        @Override
        public ItemStack execute(IBlockSource source, ItemStack stack) {
            if(ModTags.isTagged(tag,stack.getItem())){
                return super.execute(source,stack);
            }
            //vanilla behavior
            return super.customBehavior(source,stack);
        }

        @Override
        protected abstract ItemStack customBehavior(IBlockSource source, ItemStack stack);
    }

    //TODO: there must be an easier and cleaner way
    private static abstract class AdditionalDispenserBehavior extends DefaultDispenseItemBehavior {
        @Override
        public ItemStack execute(IBlockSource source, ItemStack stack) {
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

    private static class FillJarDispenserBehavior extends AdditionalDispenserBehavior {

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            TileEntity te = world.getBlockEntity(blockpos);
            if(te instanceof JarBlockTile){
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.mobHolder.isEmpty()) {
                    ItemStack returnStack;
                    if (tile.canPlaceItem(0, stack)) {
                        tile.handleAddItem(stack, null, null);
                        tile.setChanged();
                        //this.setSuccessful(true);
                        return Dispenser.glassBottleFill(source, stack, ItemStack.EMPTY);
                    }
                    else if (tile.isEmpty() && !tile.fluidHolder.isFull()) {
                        returnStack = tile.fluidHolder.interactWithItem(stack);
                        if(returnStack !=null && !returnStack.isEmpty()) {
                            tile.setChanged();
                            return Dispenser.glassBottleFill(source, stack, returnStack);
                        }
                    }
                }
                return stack;
            }
            return super.customBehavior(source,stack);
        }
    }


    private static class FlintAndSteelDispenserBehavior extends AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if(state.getBlock() instanceof LightUpBlock){
                if(LightUpBlock.lightUp(state,blockpos,world,LightUpBlock.FireSound.FLINT_AND_STEEL)){
                    if(stack.hurt(1, world.random, (ServerPlayerEntity)null)){
                        stack.setCount(0);
                    }
                }
                return stack;
            }
            return super.customBehavior(source,stack);
        }
    }


    private static class ThrowableBricksDispenserBehavior extends TaggedAdditionalDispenserBehavior{

        ThrowableBricksDispenserBehavior(ITag<Item> tag) {
            super(tag);
        }

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            World world = source.getLevel();
            IPosition iposition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            ProjectileEntity projectileentity = this.getProjectileEntity(world, iposition, stack);
            projectileentity.shoot(direction.getStepX(), (float)direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addFreshEntity(projectileentity);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(IBlockSource source) {
            source.getLevel().playSound(null, source.x()+0.5, source.y()+0.5, source.z()+0.5, SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (source.getLevel().getRandom().nextFloat() * 0.4F + 0.8F ));
        }

        protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn){
            return new ThrowableBrickEntity(worldIn, position.x(), position.y(), position.z());
        }

        protected float getProjectileInaccuracy() {
            return 7.0F;
        }
        //TODO: fix throwable bricks rendering glitchyness
        protected float getProjectileVelocity() {
            return 0.9F;
        }

    }

    private static class BombsDispenserBehavior extends ProjectileDispenseBehavior{

        @Override
        protected ProjectileEntity getProjectile(World worldIn, IPosition position, ItemStack stackIn) {
            return new BombEntity(worldIn, position.x(), position.y(), position.z());
        }
        protected float getUncertainty() {
            return 11.0F;
        }

        protected float getPower() {
            return 1.3F;
        }
    }


    private static class BambooSpikesDispenserBehavior extends AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
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

    private static class FishBucketJarDispenserBehavior extends  AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            TileEntity te = world.getBlockEntity(blockpos);
            if(te instanceof JarBlockTile){
                //TODO: add fish buckets
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.fluidHolder.isEmpty() && tile.isEmpty()) {
                    if(tile.mobHolder.interactWithBucketItem(stack,null,null)){
                        tile.setChanged();
                        return Dispenser.glassBottleFill(source, stack,new ItemStack(Items.BUCKET));
                    }
                }
                return stack;
            }

            return super.customBehavior(source,stack);
        }

    }


    private static class BucketJarDispenserBehavior extends  AdditionalDispenserBehavior{

        @Override
        protected ItemStack customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            TileEntity te = world.getBlockEntity(blockpos);
            if(te instanceof JarBlockTile){
                //TODO: add fish buckets!!
                JarBlockTile tile = ((JarBlockTile)te);
                if(tile.isEmpty())
                if(tile.mobHolder.isEmpty() && tile.isEmpty()) {
                    ItemStack returnStack = tile.fluidHolder.interactWithItem(stack);
                    if(returnStack !=null && !returnStack.isEmpty()){
                        tile.setChanged();
                        return Dispenser.glassBottleFill(source, stack,returnStack);
                    }
                }
                return stack;
            }

            return super.customBehavior(source,stack);
        }

    }

    private static class PlaceBlockDispenseBehavior extends OptionalDispenseBehavior {

        protected ItemStack execute(IBlockSource source, ItemStack stack) {
            this.setSuccess(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
                ActionResultType result = ((BlockItem)item).place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1));
                this.setSuccess(result.consumesAction());
            }
            return stack;
        }
    }

}