package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.SoundType;
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
        if (hand != player.getActiveHand())return;

        ItemStack stack = player.getHeldItem(hand);
        Item i = stack.getItem();

        if(i instanceof BlockItem && (((BlockItem) i).getBlock() instanceof LanternBlock || ((BlockItem) i).getBlock().getRegistryName().getNamespace().equals("skinnedlanterns"))){
            Direction dir = event.getFace();

            if(dir != Direction.UP && dir != Direction.DOWN){
                BlockPos pos = event.getPos();
                World world = event.getWorld();

                Item item = Registry.WALL_LANTERN_ITEM;

                BlockItemUseContext ctx = new BlockItemUseContext(
                        new ItemUseContext(player, hand, new BlockRayTraceResult(
                                new Vector3d(pos.getX(),pos.getY(),pos.getZ()), dir, pos, false)));

                ActionResultType result = ((BlockItem)item).tryPlace(ctx);

                if(result.isSuccessOrConsume()) {

                    BlockState s = ((BlockItem) i).getBlock().getDefaultState();
                    BlockPos placedPos = ctx.getPos();
                    //update light level
                    world.setBlockState(placedPos, world.getBlockState(placedPos)
                            .with(WallLanternBlock.LIGHT_LEVEL, s.getLightValue()),4|16);



                    TileEntity te = world.getTileEntity(placedPos);
                    if (te instanceof WallLanternBlockTile) {
                        ((WallLanternBlockTile) te).lanternBlock = s;
                    }

                    player.swing(hand, true);

                    SoundType soundtype = s.getSoundType(world, ctx.getPos(), player);
                    //world.playSound(null, ctx.getPos(), soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                    event.setCanceled(true);

                }
            }
        }
    }


}
