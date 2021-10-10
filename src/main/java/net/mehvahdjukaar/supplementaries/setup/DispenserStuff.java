package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.fluids.ISoftFluidConsumer;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.mehvahdjukaar.selene.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.selene.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PancakeBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.block.util.ILightable;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.common.StaticBlockItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.AmethystArrowEntity;
import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class DispenserStuff {

    public static void registerBehaviors() {

        if (!RegistryConfigs.reg.DISPENSERS.get()) return;

        //jar
        if (RegistryConfigs.reg.JAR_ENABLED.get()) {

            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.SOUL_JAR.get());
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FIREFLY_JAR.get());
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_ITEM.get());
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_TINTED.get());

            DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));

            for (Item i : CapturedMobsHelper.VALID_BUCKETS.keySet()) {
                DispenserHelper.registerCustomBehavior(new FishBucketJarDispenserBehavior(i));
            }
        }

        DispenserHelper.registerCustomBehavior(new FlintAndSteelDispenserBehavior(Items.FLINT_AND_STEEL));
        DispenserHelper.registerCustomBehavior(new BambooSpikesDispenserBehavior(Items.LINGERING_POTION));
        DispenserHelper.registerCustomBehavior(new PancakesDispenserBehavior(Items.HONEY_BOTTLE));

        if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED) {
            for(Item i : ModTags.BRICKS.getValues()){
                DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(i));
            }
        }
        //firefly
        if (RegistryConfigs.reg.FIREFLY_ENABLED.get()) {
            DispenserHelper.registerSpawnEggBehavior(ModRegistry.FIREFLY_SPAWN_EGG_ITEM.get());
        }
        //bomb
        if (RegistryConfigs.reg.BOMB_ENABLED.get()) {
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM.get(), new BombsDispenserBehavior(false));
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM.get(), new BombsDispenserBehavior(true));
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM_ON.get(), new BombsDispenserBehavior(false));
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM_ON.get(), new BombsDispenserBehavior(true));
        }
        //gunpowder
        if (ServerConfigs.cached.PLACEABLE_GUNPOWDER) {
            DispenserHelper.registerCustomBehavior(new GunpowderBehavior(Items.GUNPOWDER));
        }
        if (RegistryConfigs.reg.ROPE_ARROW_ENABLED.get()) {

            DispenserBlock.registerBehavior(ModRegistry.ROPE_ARROW_ITEM.get(), new ProjectileDispenseBehavior() {
                protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack stack) {
                    CompoundNBT com = stack.getTag();
                    int charges = stack.getMaxDamage();
                    if (com != null) {
                        if (com.contains("Damage")) {
                            charges = charges - com.getInt("Damage");
                        }
                    }
                    RopeArrowEntity arrow = new RopeArrowEntity(world, pos.x(), pos.y(), pos.z(), charges);
                    arrow.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
                    return arrow;
                }
            });

        }
        if (RegistryConfigs.reg.AMETHYST_ARROW_ENABLED.get()) {

            DispenserBlock.registerBehavior(ModRegistry.AMETHYST_ARROW_ITEM.get(), new ProjectileDispenseBehavior() {
                protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack stack) {
                    AmethystArrowEntity arrow = new AmethystArrowEntity(world, pos.x(), pos.y(), pos.z());
                    arrow.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
                    return arrow;
                }
            });
        }

    }


    private static class FlintAndSteelDispenserBehavior extends AdditionalDispenserBehavior {

        protected FlintAndSteelDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            Block block = state.getBlock();
            if (block instanceof ILightable) {
                if (((ILightable) block).lightUp(state, blockpos, world, LightUpBlock.FireSound.FLINT_AND_STEEL)) {
                    if (stack.hurt(1, world.random, null)) {
                        stack.setCount(0);
                    }
                    return ActionResult.success(stack);
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }
    }


    private static class ThrowableBricksDispenserBehavior extends AdditionalDispenserBehavior {

        protected ThrowableBricksDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            World world = source.getLevel();
            IPosition iposition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            ProjectileEntity projectileentity = this.getProjectileEntity(world, iposition, stack);
            projectileentity.shoot(direction.getStepX(), (float) direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addFreshEntity(projectileentity);
            stack.shrink(1);
            return ActionResult.success(stack);
        }

        @Override
        protected void playSound(IBlockSource source, boolean success) {
            source.getLevel().playSound(null, source.x() + 0.5, source.y() + 0.5, source.z() + 0.5, SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (source.getLevel().getRandom().nextFloat() * 0.4F + 0.8F));
        }

        protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
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

    private static class BombsDispenserBehavior extends ProjectileDispenseBehavior {

        private final boolean blue;

        public BombsDispenserBehavior(boolean blue) {
            this.blue = blue;
        }

        @Override
        protected ProjectileEntity getProjectile(World worldIn, IPosition position, ItemStack stackIn) {
            return new BombEntity(worldIn, position.x(), position.y(), position.z(), blue);
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


    private static class BambooSpikesDispenserBehavior extends AdditionalDispenserBehavior {

        protected BambooSpikesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof BambooSpikesBlock) {
                if (BambooSpikesBlock.tryAddingPotion(state, world, blockpos, stack)) {
                    return ActionResult.success(new ItemStack(Items.GLASS_BOTTLE));
                }
                return ActionResult.fail(stack);
            }

            return ActionResult.pass(stack);
        }
    }

    //TODO: generalize for fluid consumer & put into library
    private static class PancakesDispenserBehavior extends AdditionalDispenserBehavior {

        protected PancakesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            Block block = state.getBlock();
            if (block instanceof PancakeBlock) {
                if (((ISoftFluidConsumer) block).tryAcceptingFluid(world, state, blockpos, SoftFluidRegistry.HONEY, null, 1)) {
                    return ActionResult.consume(new ItemStack(Items.GLASS_BOTTLE));
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
            if (te instanceof JarBlockTile) {
                //TODO: add fish buckets
                JarBlockTile tile = ((JarBlockTile) te);
                if (tile.fluidHolder.isEmpty() && tile.isEmpty()) {
                    if (tile.mobContainer.interactWithBucket(stack, world, blockpos, null, null)) {
                        tile.setChanged();
                        return ActionResult.success(new ItemStack(Items.BUCKET));
                    }
                }
                return ActionResult.fail(stack);
            }
            return ActionResult.pass(stack);
        }
    }

    public static class GunpowderBehavior extends AdditionalDispenserBehavior {

        protected GunpowderBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResult<ItemStack> customBehavior(IBlockSource source, ItemStack stack) {

            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = source.getPos().relative(direction);
            Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
            ActionResultType result = StaticBlockItem.place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1),
                    ModRegistry.GUNPOWDER_BLOCK.get());
            if (result.consumesAction()) return ActionResult.success(stack);

            return ActionResult.fail(stack);
        }
    }

}