package net.mehvahdjukaar.supplementaries.compat.customvillagertrades;

import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.trades.VillagerTradesHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.dotcode.customvillagertrades.configs.*;
import uk.co.dotcode.customvillagertrades.events.CVTMessage;

//@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("ConstantConditions")
public class CVTHandler {

    public static void initialize(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(CVTHandler::imcSend);
    }

    public static class CVTWanderingTraderTrades extends WandererTradeCollection {


        public CVTWanderingTraderTrades() {

            this.profession = "wanderer";
            this.trades = new MyWandererTrade[]{
                    new WanderingTradeFromOffer(
                            VillagerTradesHandler.itemForEmeraldTrade(ModRegistry.GLOBE_ITEM.get(), 1, 10, 3)
                                    .getOffer(null, null), true),
                    new WanderingTradeFromOffer(
                            VillagerTradesHandler.itemForEmeraldTrade(ModRegistry.GLOBE_ITEM.get(), 1, 10, 3)
                                    .getOffer(null, null), true),
                    new WanderingTradeFromOffer(
                            VillagerTradesHandler.itemForEmeraldTrade(ModRegistry.FLAX_SEEDS_ITEM.get(), 1, 6, 8)
                                    .getOffer(null, null), false)
            };
        }

    }

    public static class CVTVillagerTrades extends TradeCollection {

        public CVTVillagerTrades() {

            this.profession = VillagerProfession.FARMER.toString();
            this.trades = new TradeFromOffer[]{
                    new TradeFromOffer(
                            (new BasicTrade(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get(), 15),
                                    new ItemStack(net.minecraft.item.Items.EMERALD),
                                    16, 2, 0.05f)).getOffer(null, null), 3)
            };

        }

    }

    private static class TradeFromOffer extends MyTrade {

        public TradeFromOffer(MerchantOffer offer, int level) {
            this.maxUses = offer.getMaxUses();
            this.request = new TradeFromItem(offer.getBaseCostA());
            this.offer = new TradeFromItem(offer.getResult());
            this.tradeExp = offer.getXp();
            this.demand = offer.getDemand();
            this.priceMultiplier = offer.getPriceMultiplier();
            this.tradeLevel = level;
        }
    }

    private static class WanderingTradeFromOffer extends MyWandererTrade {

        public WanderingTradeFromOffer(MerchantOffer offer, boolean rare) {
            this.isRare = rare;
            this.maxUses = offer.getMaxUses();
            this.request = new TradeFromItem(offer.getBaseCostA());
            this.offer = new TradeFromItem(offer.getResult());
            this.tradeExp = offer.getXp();
            this.demand = offer.getDemand();
            this.priceMultiplier = offer.getPriceMultiplier();
        }
    }

    private static class TradeFromItem extends MyTradeItem {
        public TradeFromItem(ItemStack stack) {
            super(stack.getItem().getRegistryName().toString(), stack.getCount(), 0);
        }
    }

    public static void imcSend(final InterModEnqueueEvent event) {
        if(RegistryConfigs.reg.GLOBE_ENABLED.get())
        InterModComms.sendTo("customvillagertrades", CVTMessage.ADD_WANDERER_TRADES, CVTWanderingTraderTrades::new);
        if(RegistryConfigs.reg.FLAX_ENABLED.get())
        InterModComms.sendTo("customvillagertrades", CVTMessage.ADD_VILLAGER_TRADES, CVTVillagerTrades::new);
    }
}
