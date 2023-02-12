package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DispenserBehaviorsManager {


    public static void registerBehaviors() {
        boolean isForge = PlatformHelper.getPlatform().isForge();

        if (!CommonConfigs.General.DISPENSERS.get()) return;

        if (CommonConfigs.Building.PANCAKES_ENABLED.get() && CompatHandler.QUARK && QuarkCompat.isJukeboxModuleOn()) {
            DispenserBlock.registerBehavior(ModRegistry.PANCAKE.get(), new PancakeDiscBehavior());
        }

        if (CommonConfigs.Tweaks.ENDER_PEAR_DISPENSERS.get()) {
            DispenserHelper.registerCustomBehavior(new EnderPearlBehavior());
        }
        DispenserBlock.registerBehavior(ModRegistry.DISPENSER_MINECART_ITEM.get(), DispenserMinecartItem.DISPENSE_ITEM_BEHAVIOR);

        DispenseItemBehavior armorBehavior = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(source, stack));
                return stack;
            }
        };
        DispenserBlock.registerBehavior(ModRegistry.ENDERMAN_SKULL_ITEM.get(), armorBehavior);

        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FODDER.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BUBBLE_BLOCK.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.SACK.get());
        DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_ITEM.get());

        DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));
        DispenserHelper.registerCustomBehavior(new FlintAndSteelBehavior(Items.FLINT_AND_STEEL));
        DispenserHelper.registerCustomBehavior(new BambooSpikesBehavior(Items.LINGERING_POTION));
        DispenserHelper.registerCustomBehavior(new PancakeBehavior(Items.HONEY_BOTTLE));
        if (isForge) {
            DispenserHelper.registerCustomBehavior(new FakePlayerUseItemBehavior(ModRegistry.SOAP.get()));
        }

        if (CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get()) {
            Registry.ITEM.getTagOrEmpty(ModTags.BRICKS).iterator().forEachRemaining(h ->
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
        boolean jar = CommonConfigs.Utilities.JAR_ENABLED.get();

        if (axe || jar) {
            for (Item i : Registry.ITEM) {
                try {
                    if (jar && BucketHelper.isFishBucket(i)) {
                        DispenserHelper.registerCustomBehavior(new FishBucketJarBehavior(i));
                    }
                    if (isForge && axe && i instanceof AxeItem) {
                        DispenserHelper.registerCustomBehavior(new FakePlayerUseItemBehavior(i));
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Error white registering dispenser behavior for item {}: {}", i, e);
                }
            }
        }
    }

    //TODO: generalize for fluid consumer & put into library

}