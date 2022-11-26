package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.forge.ClientEventsForge;
import net.mehvahdjukaar.supplementaries.common.events.forge.ServerEventsForge;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MehVahdJukaar
 */
@Mod(Supplementaries.MOD_ID)
public class SupplementariesForge {


    public SupplementariesForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Supplementaries.commonInit();

        bus.addListener(SupplementariesForge::setup);
        bus.addListener(SupplementariesForge::registerOverrides);

        ServerEventsForge.init();
        ModLootModifiers.init();

        if (PlatformHelper.getEnv().isClient()) {
            ClientRegistry.init();
            ClientEventsForge.init();
        }
    }

    public static void registerOverrides(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.ITEMS.getRegistryKey()) {
            if (RegistryConfigs.SHULKER_HELMET_ENABLED.get()) {

                event.getForgeRegistry().register(new ResourceLocation("minecraft:shulker_shell"),
                        new ShulkerShellItem(new Item.Properties()
                                .stacksTo(64)
                                .tab(CreativeModeTab.TAB_MATERIALS)));
            }
        }
    }


    public static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Supplementaries::commonSetup);
    }

    public static final ToolAction SOAP_CLEAN = ToolAction.get("soap_clean");


    public static class b extends Block{

        public b(Properties arg) {
            super(arg);
        }
        //TODO: use
        @Override
        public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
            return super.getAppearance(state, level, pos, side, queryState, queryPos);
        }
    }
}
