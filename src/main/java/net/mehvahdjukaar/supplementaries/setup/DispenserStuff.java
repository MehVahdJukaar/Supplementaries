package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.mehvahdjukaar.selene.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.selene.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class DispenserStuff {

    public static void registerBehaviors() {

        if(!RegistryConfigs.reg.DISPENSERS.get())return;

        //jar
        if(RegistryConfigs.reg.JAR_ENABLED.get()){

            DispenserHelper.registerPlaceBlockBehavior(Registry.SOUL_JAR.get());
            DispenserHelper.registerPlaceBlockBehavior(Registry.FIREFLY_JAR.get());
            DispenserHelper.registerPlaceBlockBehavior(Registry.EMPTY_JAR_ITEM.get());
            DispenserHelper.registerPlaceBlockBehavior(Registry.JAR_ITEM.get());

            DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));

            for(Item i : CapturedMobsHelper.VALID_BUCKETS.keySet()){
                DispenserHelper.registerCustomBehavior(new FishBucketJarDispenserBehavior(i));
            }
        }

        DispenserHelper.registerCustomBehavior(new FlintAndSteelDispenserBehavior(Items.FLINT_AND_STEEL));
        DispenserHelper.registerCustomBehavior(new BambooSpikesDispenserBehavior(Items.LINGERING_POTION));

        if(ServerConfigs.cached.THROWABLE_BRICKS_ENABLED){
            DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(Items.NETHER_BRICK));
            DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(Items.BRICK));
        }
        //firefly
        if(RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            DispenserHelper.registerSpawnEggBehavior(Registry.FIREFLY_SPAWN_EGG_ITEM.get());
        }
        if(RegistryConfigs.reg.BOMB_ENABLED.get()){
            DispenserBlock.registerBehavior(Registry.BOMB_ITEM.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_BLUE_ITEM.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_ITEM_ON.get(), new BombsDispenserBehavior());
            DispenserBlock.registerBehavior(Registry.BOMB_BLUE_ITEM_ON.get(), new BombsDispenserBehavior());
        }
    }


    private static class FlintAndSteelDispenserBehavior extends AdditionalDispenserBehavior{

        protected FlintAndSteelDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if(state.getBlock() instanceof LightUpBlock){
                if(LightUpBlock.lightUp(state,blockpos,world,LightUpBlock.FireSound.FLINT_AND_STEEL)){
                    if(stack.hurt(1, world.random, (ServerPlayerEntity)null)){
                        stack.setCount(0);
                    }
                    return ActionResult.success(stack);
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }
    }


    private static class ThrowableBricksDispenserBehavior extends AdditionalDispenserBehavior{

        protected ThrowableBricksDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            World world = source.getLevel();
            IPosition iposition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            ProjectileEntity projectileentity = this.getProjectileEntity(world, iposition, stack);
            projectileentity.shoot(direction.getStepX(), (float)direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addFreshEntity(projectileentity);
            stack.shrink(1);
            return ActionResult.success(stack);
        }

        @Override
        protected void playSound(IBlockSource source, boolean success) {
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
        @Override
        protected float getUncertainty() {
            return 11.0F;
        }

        @Override
        protected float getPower() {
            return 1.3F;
        }
    }


    private static class BambooSpikesDispenserBehavior extends AdditionalDispenserBehavior{

        protected BambooSpikesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if(state.getBlock() instanceof BambooSpikesBlock){
                if(BambooSpikesBlock.tryAddingPotion(state,world,blockpos,stack)){
                    return ActionResult.success(new ItemStack(Items.GLASS_BOTTLE));
                }
                return ActionResult.fail(stack);
            }

            return ActionResult.pass(stack);
        }
    }

    private static class FishBucketJarDispenserBehavior extends AdditionalDispenserBehavior {

        protected FishBucketJarDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
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
                        return ActionResult.success(new ItemStack(Items.BUCKET));
                    }
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }

    }

}