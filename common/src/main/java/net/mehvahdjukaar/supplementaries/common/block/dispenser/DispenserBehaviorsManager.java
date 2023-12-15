package net.mehvahdjukaar.supplementaries.common.block.dispenser;


import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DispenserBehaviorsManager {


    //hacky. uses default tags to register stuff like bricks and fluids. wont properly work with reloads
    //TODO: make a reload safe dispenser map for these
    public static void registerBehaviors(RegistryAccess registryAccess) {
        boolean isForge = PlatHelper.getPlatform().isForge();

        if (!CommonConfigs.General.DISPENSERS.get()) return;

        for (SoftFluid f : SoftFluidRegistry.getRegistry(registryAccess)) {
            registerFluidBehavior(f);
        }

        if (CommonConfigs.Building.PANCAKES_ENABLED.get()) {
            DispenserHelper.registerCustomBehavior(new PancakeBehavior(Items.HONEY_BOTTLE));

            DispenserBlock.registerBehavior(ModRegistry.PANCAKE.get(), new PancakeDiscBehavior());
        }

        if (CommonConfigs.Tweaks.ENDER_PEAR_DISPENSERS.get()) {
            DispenserHelper.registerCustomBehavior(new EnderPearlBehavior());
        }
        if (CommonConfigs.Redstone.DISPENSER_MINECART_ENABLED.get()) {
            DispenserBlock.registerBehavior(ModRegistry.DISPENSER_MINECART_ITEM.get(), DispenserMinecartItem.DISPENSE_ITEM_BEHAVIOR);
        }
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get()) {
            DispenseItemBehavior armorBehavior = new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    this.setSuccess(ArmorItem.dispenseArmor(source, stack));
                    return stack;
                }
            };
            DispenserBlock.registerBehavior(ModRegistry.ENDERMAN_SKULL_ITEM.get(), armorBehavior);
        }
        if (CommonConfigs.Functional.FODDER_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FODDER.get());
        }
        if (CommonConfigs.Functional.SOAP_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BUBBLE_BLOCK.get());
        }
        if (CommonConfigs.Functional.SACK_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.SACK.get());
        }
        if (CommonConfigs.Functional.JAR_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_ITEM.get());
            DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));
        }
        DispenserHelper.registerCustomBehavior(new FlintAndSteelBehavior(Items.FLINT_AND_STEEL));
        if (CommonConfigs.Functional.BAMBOO_SPIKES_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BAMBOO_SPIKES_ITEM.get());
        }
        if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
            DispenserHelper.registerCustomBehavior(new BambooSpikesBehavior(Items.LINGERING_POTION));
        }
        if (isForge) {
            DispenserHelper.registerCustomBehavior(new FakePlayerUseItemBehavior(ModRegistry.SOAP.get()));
        }

        if (CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get()) {
            BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.BRICKS).iterator().forEachRemaining(h ->
                    DispenserHelper.registerCustomBehavior(new ThrowableBricksBehavior(h.value()))
            );
        }
        //bomb
        if (CommonConfigs.Tools.BOMB_ENABLED.get()) {
            //default behaviors for modded items
            var bombBehavior = new BombsBehavior();
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
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {

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
        boolean jar = CommonConfigs.Functional.JAR_ENABLED.get();
        boolean key = CommonConfigs.isEnabled(ModConstants.KEY_NAME);

        if (axe || jar || key) {
            for (Item i : BuiltInRegistries.ITEM) {
                try {
                    if (jar && BucketHelper.isFishBucket(i)) {
                        DispenserHelper.registerCustomBehavior(new FishBucketJarBehavior(i));
                    }
                    if (isForge && axe && i instanceof AxeItem) {
                        DispenserHelper.registerCustomBehavior(new FakePlayerUseItemBehavior(i));
                    }
                    if(key && i instanceof KeyItem){
                        DispenserHelper.registerCustomBehavior(new KeyBehavior(i));
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Error white registering dispenser behavior for item {}: {}", i, e);
                }
            }
        }
    }

    //TODO: addback
    public static void registerFluidBehavior(SoftFluid f) {
        Set<Item> itemSet = new HashSet<>();
        Collection<FluidContainerList.Category> categories = f.getContainerList().getCategories();
        for (FluidContainerList.Category c : categories) {
            Item empty = c.getEmptyContainer();
            //prevents registering stuff twice
            if (empty != Items.AIR && !itemSet.contains(empty)) {
                DispenserHelper.registerCustomBehavior(new FillFluidHolderBehavior(empty));
                itemSet.add(empty);
            }
            for (Item full : c.getFilledItems()) {
                if (full != Items.AIR && !itemSet.contains(full)) {
                    DispenserHelper.registerCustomBehavior(new FillFluidHolderBehavior(full));
                    itemSet.add(full);
                }
            }
        }
    }

}