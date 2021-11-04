package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.mehvahdjukaar.selene.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.selene.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PancakeBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.block.util.ILightable;
import net.mehvahdjukaar.supplementaries.common.BlockItemUtils;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserStuff {

    public static void registerBehaviors() {

        if (!RegistryConfigs.reg.DISPENSERS.get()) return;

        //fodder
        if(RegistryConfigs.reg.FODDER_ENABLED.get()){
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FODDER.get());
        }

        //jar
        if (RegistryConfigs.reg.JAR_ENABLED.get()) {

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
            for (Item i : ModTags.BRICKS.getValues()) {
                DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(i));
            }
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

            DispenserBlock.registerBehavior(ModRegistry.ROPE_ARROW_ITEM.get(), new AbstractProjectileDispenseBehavior() {
                protected Projectile getProjectile(Level world, Position pos, ItemStack stack) {
                    CompoundTag com = stack.getTag();
                    int charges = stack.getMaxDamage();
                    if (com != null) {
                        if (com.contains("Damage")) {
                            charges = charges - com.getInt("Damage");
                        }
                    }
                    RopeArrowEntity arrow = new RopeArrowEntity(world, pos.x(), pos.y(), pos.z(), charges);
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
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
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof ILightable block) {
                if (block.lightUp(state, blockpos, world, LightUpBlock.FireSound.FLINT_AND_STEEL)) {
                    if (stack.hurt(1, world.random, null)) {
                        stack.setCount(0);
                    }
                    return InteractionResultHolder.success(stack);
                }
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    }


    private static class ThrowableBricksDispenserBehavior extends AdditionalDispenserBehavior {

        protected ThrowableBricksDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            Level world = source.getLevel();
            Position dispensePosition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            Projectile projectileEntity = this.getProjectileEntity(world, dispensePosition, stack);
            projectileEntity.shoot(direction.getStepX(), (float) direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addFreshEntity(projectileEntity);
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        @Override
        protected void playSound(BlockSource source, boolean success) {
            source.getLevel().playSound(null, source.x() + 0.5, source.y() + 0.5, source.z() + 0.5, SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (source.getLevel().getRandom().nextFloat() * 0.4F + 0.8F));
        }

        protected Projectile getProjectileEntity(Level worldIn, Position position, ItemStack stackIn) {
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

    private static class BombsDispenserBehavior extends AbstractProjectileDispenseBehavior {

        private final boolean blue;

        public BombsDispenserBehavior(boolean blue) {
            this.blue = blue;
        }

        @Override
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
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
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof BambooSpikesBlock) {
                if (BambooSpikesBlock.tryAddingPotion(state, world, blockpos, stack)) {
                    return InteractionResultHolder.success(new ItemStack(Items.GLASS_BOTTLE));
                }
                return InteractionResultHolder.fail(stack);
            }

            return InteractionResultHolder.pass(stack);
        }
    }

    //TODO: generalize for fluid consumer & put into library
    private static class PancakesDispenserBehavior extends AdditionalDispenserBehavior {

        protected PancakesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof PancakeBlock block) {
                if (block.tryAcceptingFluid(world, state, blockpos, SoftFluidRegistry.HONEY, null, 1)) {
                    return InteractionResultHolder.consume(new ItemStack(Items.GLASS_BOTTLE));
                }
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    }

    private static class FishBucketJarDispenserBehavior extends AdditionalDispenserBehavior {

        protected FishBucketJarDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel world = source.getLevel();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (world.getBlockEntity(blockpos) instanceof JarBlockTile tile) {
                //TODO: add fish buckets
                if (tile.fluidHolder.isEmpty() && tile.isEmpty()) {
                    if (tile.mobContainer.interactWithBucket(stack, world, blockpos, null, null)) {
                        tile.setChanged();
                        return InteractionResultHolder.success(new ItemStack(Items.BUCKET));
                    }
                }
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    }

    public static class GunpowderBehavior extends AdditionalDispenserBehavior {

        protected GunpowderBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {

            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos blockpos = source.getPos().relative(direction);
            Direction direction1 = source.getLevel().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
            InteractionResult result = BlockItemUtils.place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1),
                    ModRegistry.GUNPOWDER_BLOCK.get());
            if (result.consumesAction()) return InteractionResultHolder.success(stack);

            return InteractionResultHolder.fail(stack);
        }
    }

}