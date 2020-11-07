package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.setup.ClientSetup;
import net.mehvahdjukaar.supplementaries.setup.Dispenser;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Supplementaries.MOD_ID)
public class Supplementaries{

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public Supplementaries() {

        Registry.init();

        Networking.registerMessages();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }




    //I should probably mode this outta here
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();

        if(!player.abilities.allowEdit)return;

        Hand hand = event.getHand();
        if (hand != player.getActiveHand())return;

        ItemStack stack = player.getHeldItem(hand);
        Item i = stack.getItem();
        if(i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof LanternBlock){
            Direction dir = event.getFace();

            if(dir != Direction.UP && dir != Direction.DOWN){
                BlockPos pos = event.getPos();
                World world = event.getWorld();

                Item item = Registry.WALL_LANTERN_ITEM.get();

                BlockItemUseContext ctx = new BlockItemUseContext(
                        new ItemUseContext(player, hand, new BlockRayTraceResult(
                                new Vector3d(pos.getX(),pos.getY(),pos.getZ()), dir, pos, false)));

                ActionResultType result = ((BlockItem)item).tryPlace(ctx);

                if(result.isSuccessOrConsume()) {
                    if (player.isCreative()) stack.grow(1);

                    BlockState s = ((BlockItem) i).getBlock().getDefaultState();

                    TileEntity te = world.getTileEntity(ctx.getPos());
                    if (te instanceof WallLanternBlockTile) {
                        ((WallLanternBlockTile) te).lanternBlock = s;
                    }

                    player.swing(hand, true);

                    SoundType soundtype = s.getSoundType(world, ctx.getPos(), player);
                    world.playSound(player, ctx.getPos(), soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);


                }
            }
        }
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
