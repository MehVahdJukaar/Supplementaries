package net.mehvahdjukaar.supplementaries.common.block.dispenser;


import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.PopperBehavior;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.KeyItem;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class DispenserBehaviorsManager {

    public static void init(){
        RegHelper.addDynamicDispenserBehaviorRegistration(DispenserBehaviorsManager::registerBehaviors);
    }

    public static void registerBehaviors(DispenserHelper.Event event) {
        boolean isForge = PlatHelper.getPlatform().isForge();

        if (!CommonConfigs.General.DISPENSERS.get()) return;

        if(CommonConfigs.Tweaks.BUNDLE_DISPENSER.get()){
            event.register(new EmptyBundleItemBehavior(Items.BUNDLE));
            event.register(new EmptyContainerItemBehavior(ModRegistry.LUNCH_BASKET_ITEM.get()));
            event.register(new EmptyContainerItemBehavior(ModRegistry.QUIVER_ITEM.get()));
        }

        if (CommonConfigs.Building.PANCAKES_ENABLED.get()) {
            event.register(new PancakeBehavior(Items.HONEY_BOTTLE));
        }

        if (CommonConfigs.Tweaks.ENDER_PEAR_DISPENSERS.get()) {
            event.register(new ThrowableEnderPearlBehavior());
        }
        if (CommonConfigs.Redstone.DISPENSER_MINECART_ENABLED.get()) {
            event.register(ModRegistry.DISPENSER_MINECART_ITEM.get(), DispenserMinecartItem.DISPENSE_ITEM_BEHAVIOR);
        }
        //TODO: shulker shell
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_ENABLED.get()) {
            DispenseItemBehavior armorBehavior = new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    this.setSuccess(ArmorItem.dispenseArmor(source, stack));
                    return stack;
                }
            };
            event.register(ModRegistry.ENDERMAN_SKULL_ITEM.get(), armorBehavior);
        }
        if (CommonConfigs.Functional.FODDER_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.FODDER.get());
        }
        if (CommonConfigs.Tools.LUNCH_BOX_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.LUNCH_BASKET.get());
        }
        if (CommonConfigs.Functional.SOAP_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.BUBBLE_BLOCK.get());
        }
        if (CommonConfigs.Functional.SACK_ENABLED.get()) {
            for (var s : SackBlock.SACK_BLOCKS) {
                event.registerPlaceBlock(s);
            }
        }
        if (CommonConfigs.Functional.JAR_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.JAR_ITEM.get());
            event.register(new AddItemToInventoryBehavior(Items.COOKIE));
        }
        event.register(new FlintAndSteelBehavior(Items.FLINT_AND_STEEL));
        if (CommonConfigs.Functional.BAMBOO_SPIKES_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.BAMBOO_SPIKES.get());
        }
        if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get()) {
            event.registerPlaceBlock(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
            event.register(new BambooSpikesBehavior(Items.LINGERING_POTION));
        }
        if (isForge) {
            event.register(new FakePlayerUseItemBehavior(ModRegistry.SOAP.get()));
        }

        if (CommonConfigs.Tools.POPPER_ENABLED.get()) {
            event.register(
                    new FireBehaviorProxy(ModRegistry.CONFETTI_POPPER.get(),
                            new PopperBehavior(), 0.7f, 1, false));
        }

        if (CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get()) {
            BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.BRICKS).iterator().forEachRemaining(h ->
                    event.register(new ThrowableBricksBehavior(h.value()))
            );
        }
        //bomb
        if (CommonConfigs.Tools.BOMB_ENABLED.get()) {
            //default behaviors for modded items
            var bombBehavior = new BombsBehavior();
            event.register(ModRegistry.BOMB_ITEM.get(), bombBehavior);
            event.register(ModRegistry.BOMB_BLUE_ITEM.get(), bombBehavior);
            event.register(ModRegistry.BOMB_SPIKY_ITEM.get(), bombBehavior);
        }
        //gunpowder
        if (CommonConfigs.Tweaks.PLACEABLE_GUNPOWDER.get()) {
            event.register(new GunpowderBehavior(Items.GUNPOWDER));
        }
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {

            event.register(ModRegistry.ROPE_ARROW_ITEM.get(), new AbstractProjectileDispenseBehavior() {
                private Projectile getProjectile(Level world, Position pos, ItemStack stack) {
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
        boolean slimeball = CommonConfigs.isEnabled(ModConstants.KEY_NAME);

        if (axe || jar || key) {
            for (Item i : BuiltInRegistries.ITEM) {
                try {
                    if (jar && BucketHelper.isFishBucket(i)) {
                        event.register(new FishBucketJarBehavior(i));
                    }
                    if (isForge && axe && i instanceof AxeItem) {
                        event.register(new FakePlayerUseItemBehavior(i));
                    }
                    if (key && i instanceof KeyItem) {
                        event.register(new KeyBehavior(i));
                    }
                    if (slimeball && SuppPlatformStuff.isSlimeball(i)) {
                        event.register(new ThrowableSlimeballBehavior(i));
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Error white registering dispenser behavior for item {}: {}", i, e);
                }
            }
        }
    }

}