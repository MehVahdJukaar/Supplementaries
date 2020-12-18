package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class RightClickEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if(!ServerConfigs.cached.WALL_LANTERN_PLACEMENT)return;
        PlayerEntity player = event.getPlayer();
        if(!player.abilities.allowEdit)return;

        Hand hand = event.getHand();
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        //is lantern
        if(i instanceof BlockItem && (((BlockItem) i).getBlock() instanceof LanternBlock || ((BlockItem) i).getBlock().getRegistryName().getNamespace().equals("skinnedlanterns"))) {
            Direction dir = event.getFace();
            //is wall face
            if (dir != Direction.UP && dir != Direction.DOWN && dir != null) {

                World worldIn = event.getWorld();
                BlockPos pos = event.getPos();
                BlockState blockstate = worldIn.getBlockState(pos);
                //try interact with block behind

                BlockRayTraceResult raytrace = new BlockRayTraceResult(
                        new Vector3d(pos.getX(), pos.getY(), pos.getZ()), dir, pos, false);

               if(!player.isSneaking()) {
                    ActionResultType interactresult = blockstate.onBlockActivated(worldIn, player, hand, raytrace);
                    //interacted with block
                    if (interactresult.isSuccessOrConsume()) {
                        if (player instanceof ServerPlayerEntity)
                            CriteriaTriggers.RIGHT_CLICK_BLOCK_WITH_ITEM.test((ServerPlayerEntity) player, pos, stack);
                        event.setCanceled(true);
                        event.setCancellationResult(interactresult);
                        return;
                    }
                }

                //place lantern
                Item item = Registry.WALL_LANTERN_ITEM;

                BlockItemUseContext ctx = new BlockItemUseContext(
                        new ItemUseContext(player, hand, raytrace));
                ActionResultType placeresult = ((BlockItem)item).tryPlace(ctx);

                if(placeresult.isSuccessOrConsume()){
                    //swing animation
                    if(player instanceof ServerPlayerEntity)
                        CriteriaTriggers.RIGHT_CLICK_BLOCK_WITH_ITEM.test((ServerPlayerEntity) player, pos, stack);
                    event.setCanceled(true);
                    event.setCancellationResult(placeresult);

                    //update tile
                    BlockState s = ((BlockItem) i).getBlock().getDefaultState();
                    BlockPos placedpos = ctx.getPos();
                    TileEntity te = worldIn.getTileEntity(placedpos);
                    if (te instanceof WallLanternBlockTile) {
                        ((WallLanternBlockTile) te).lanternBlock = s;
                        worldIn.setBlockState(placedpos, worldIn.getBlockState(placedpos)
                                .with(WallLanternBlock.LIGHT_LEVEL, s.getLightValue()),4|16);
                    }
                }
            }
        }
    }


}
