package net.mehvahdjukaar.supplementaries.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.neoforge.ServerEventsForge;
import net.mehvahdjukaar.supplementaries.common.items.ShulkerShellItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {

    public static class  e extends Item{
        public e(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
            return super.isBookEnchantable(stack, book);
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return super.isEnchantable(stack);
        }

        @Override
        public ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
            return super.applyEnchantments(stack, enchantments);
        }

        @Override
        public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
            return super.getEnchantmentLevel(stack, enchantment);
        }

        @Override
        public int getEnchantmentValue(ItemStack stack) {
            return super.getEnchantmentValue(stack);
        }



        @Override
        public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
            return super.getAllEnchantments(stack, lookup);
        }
    }

    public SupplementariesForge(IEventBus bus) {
        Supplementaries.commonInit();

        bus.register(this);

        ServerEventsForge.init();
        VillagerScareStuff.init();

        PlatHelper.getPhysicalSide().ifClient(() -> {
            ClientRegistry.init();
            ClientEventsForge.init();
        });
    }


    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityHandler.register(event);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        VillagerScareStuff.setup();
    }

    @SubscribeEvent
    public void registerOverrides(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.ITEM.getRegistryKey()) {
            if (CommonConfigs.Tweaks.SHULKER_HELMET_ENABLED.get()) {
                event.getForgeRegistry().register(new ResourceLocation("minecraft:shulker_shell"),
                        new ShulkerShellItem(new Item.Properties()
                                .stacksTo(64)
                                ));
            }
        }
    }


    public static final ItemAbility SOAP_CLEAN = ItemAbility.get("soap_clean");



}
