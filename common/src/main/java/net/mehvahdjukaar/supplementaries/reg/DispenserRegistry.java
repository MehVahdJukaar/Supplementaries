package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PancakeBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class DispenserRegistry {


    public static void registerBehaviors() {
        boolean isForge = PlatformHelper.getPlatform().isForge();

        if (!RegistryConfigs.DISPENSERS.get()) return;

        if (RegistryConfigs.PANCAKES_ENABLED.get() && CompatHandler.QUARK && QuarkCompat.isJukeboxModuleOn()) {
            DispenserBlock.registerBehavior(ModRegistry.PANCAKE.get(), new PancakeDiscBehavior());
        }

        if (CommonConfigs.Tweaks.ENDER_PEAR_DISPENSERS.get()) {
            DispenserHelper.registerCustomBehavior(new EnderPearlBehavior());
        }
        DispenserBlock.registerBehavior(ModRegistry.DISPENSER_MINECART_ITEM.get(), DispenserMinecartItem.DISPENSE_ITEM_BEHAVIOR);

        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FODDER.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BUBBLE_BLOCK.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.SACK.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_ITEM.get());

        DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));
        DispenserHelper.registerCustomBehavior(new FlintAndSteelDispenserBehavior(Items.FLINT_AND_STEEL));
        DispenserHelper.registerCustomBehavior(new BambooSpikesDispenserBehavior(Items.LINGERING_POTION));
        DispenserHelper.registerCustomBehavior(new PancakesDispenserBehavior(Items.HONEY_BOTTLE));
        if(isForge) {
            DispenserHelper.registerCustomBehavior(new SoapBehavior(ModRegistry.SOAP.get()));
        }

        if (CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get()) {
            Registry.ITEM.getTagOrEmpty(ModTags.BRICKS).iterator().forEachRemaining(h ->
                    DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(h.value()))
            );
        }
        //bomb
        if (RegistryConfigs.BOMB_ENABLED.get()) {
            //default behaviors for modded items
            var bombBehavior = new BombsDispenserBehavior();
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM_ON.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM_ON.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM_ON.get(), bombBehavior);
        }
        //gunpowder
        if (CommonConfigs.Tweaks.PLACEABLE_GUNPOWDER.get()) {
            DispenserHelper.registerCustomBehavior(new GunpowderBehavior(Items.GUNPOWDER));
        }
        if (RegistryConfigs.ROPE_ARROW_ENABLED.get()) {

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

        boolean axe = CommonConfigs.Tweaks.AXE_DISPENSER_BEHAVIORS.get();
        boolean jar = RegistryConfigs.JAR_ENABLED.get();

        if (axe || jar) {
            for (Item i : Registry.ITEM) {
                try {
                    if (jar && BucketHelper.isFishBucket(i)) {
                        DispenserHelper.registerCustomBehavior(new FishBucketJarDispenserBehavior(i));
                    }
                    if (isForge && axe && i instanceof AxeItem) {
                        DispenserHelper.registerCustomBehavior(new AxeDispenserBehavior(i));
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Error white registering dispenser behavior for item {}: {}", i, e);
                }
            }
        }
    }

    private static class AxeDispenserBehavior extends AdditionalDispenserBehavior {

        protected AxeDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel level = source.getLevel();
            Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos pos = source.getPos().relative(dir);

            Player fp = CommonUtil.getFakePlayer(level);
            fp.setItemInHand(InteractionHand.MAIN_HAND, stack);
            UseOnContext context = new UseOnContext(fp, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), dir, pos, false));

            var v = stack.useOn(context);
            if(v.consumesAction())return InteractionResultHolder.sidedSuccess( stack,false);
            return InteractionResultHolder.fail(stack);
        }
    }

    private static class SoapBehavior extends AdditionalDispenserBehavior {

        protected SoapBehavior(Item item) {
            super(item);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerLevel level = source.getLevel();
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (SoapItem.tryCleaning(stack, level, pos, null)) {
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.fail(stack);
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
                if (block.lightUp(null, state, blockpos, world, ILightable.FireSourceType.FLINT_AND_STEEL)) {
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
            projectileEntity.shoot(direction.getStepX(),  direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
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

    public static class PancakeDiscBehavior extends OptionalDispenseItemBehavior {

        @Override
        @Nonnull
        protected ItemStack execute(BlockSource source, @Nonnull ItemStack stack) {
            Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos pos = source.getPos().relative(dir);
            Level world = source.getLevel();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.JUKEBOX) {
                if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                    ItemStack currentRecord = jukebox.getRecord();
                    ((JukeboxBlock) state.getBlock()).setRecord(null, world, pos, state, stack);
                    world.levelEvent(null, 1010, pos, Item.getId(ModRegistry.PANCAKE_DISC.get()));
                    return currentRecord;
                }
            }
            return super.execute(source, stack);
        }
    }

    private static class BombsDispenserBehavior extends AbstractProjectileDispenseBehavior {

        @Override
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
            return new BombEntity(worldIn, position.x(), position.y(), position.z(), ((BombItem) stackIn.getItem()).getType());
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
                if (block.tryAcceptingFluid(world, state, blockpos, VanillaSoftFluids.HONEY.get(), null, 1)) {
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
            InteractionResult result = ItemsUtil.place(new DirectionalPlaceContext(source.getLevel(), blockpos, direction, stack, direction1),
                    ModRegistry.GUNPOWDER_BLOCK.get());
            if (result.consumesAction()) return InteractionResultHolder.success(stack);

            return InteractionResultHolder.fail(stack);
        }
    }

    public static class EnderPearlBehavior extends AdditionalDispenserBehavior {

        protected EnderPearlBehavior() {
            super(Items.ENDER_PEARL);
        }

        @Override
        protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            Level level = source.getLevel();
            BlockPos pos = source.getPos();

            ThrownEnderpearl pearl = PearlMarker.getPearlToDispense(source, level, pos);


            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);

            pearl.shoot(direction.getStepX(),  direction.getStepY() + 0.1F, direction.getStepZ(), this.getPower(), this.getUncertainty());
            level.addFreshEntity(pearl);

            stack.shrink(1);

            return InteractionResultHolder.success(stack);
        }


        @Override
        protected void playSound(BlockSource source, boolean success) {
            source.getLevel().levelEvent(1002, source.getPos(), 0);
        }

        protected float getUncertainty() {
            return 6.0F;
        }

        protected float getPower() {
            return 1.1F;
        }
    }

}