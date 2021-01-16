package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;

import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.BellTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class RightClickEvent {


    private static boolean isLantern(Item i){
        if(i instanceof BlockItem){
            Block b =  ((BlockItem) i).getBlock();
            String namespace = b.getRegistryName().getNamespace();
            return ((b instanceof LanternBlock || namespace.equals("skinnedlanterns"))
                    && !ServerConfigs.cached.WALL_LANTERN_BLACKLIST.contains(namespace));
        }
        return false;
    }

    private static boolean isBrick(Item i){
        return ((Tags.Items.INGOTS_BRICK!=null&&i.isIn(Tags.Items.INGOTS_BRICK))
                ||(Tags.Items.INGOTS_NETHER_BRICK!=null&&i.isIn(Tags.Items.INGOTS_NETHER_BRICK)));
    }


    private static boolean findConnectedBell(World world, BlockPos pos, PlayerEntity player, int it){
        if(it>ServerConfigs.cached.BELL_CHAIN_LENGTH)return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if(b instanceof ChainBlock){
            return findConnectedBell(world,pos.up(),player,it+1);
        }
        else if(b instanceof BellBlock && it !=0){
            boolean success = ((BellBlock) b).ring(world, pos, state.get(BellBlock.HORIZONTAL_FACING).rotateY());
            if (success && player != null) {
                player.addStat(Stats.BELL_RING);
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        if(stack.isEmpty()){
            if(!ServerConfigs.cached.BELL_CHAIN)return;
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            if(findConnectedBell(world,pos,player,0)){
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.func_233537_a_(world.isRemote));
            }
            return;
        }

        if(!ServerConfigs.cached.WALL_LANTERN_PLACEMENT)return;

        if(!player.abilities.allowEdit)return;


        //is lantern
        if(isLantern(i)) {
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





    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(!ServerConfigs.cached.THROWABLE_BRICKS)return;
        PlayerEntity playerIn = event.getPlayer();
        Hand handIn = event.getHand();
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        Item i = itemstack.getItem();
        if(isBrick(i)) {
            World worldIn = event.getWorld();
            worldIn.playSound(null, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F ));
            if (!worldIn.isRemote) {
                ThrowableBrickEntity brickEntity = new ThrowableBrickEntity(worldIn, playerIn);
                brickEntity.setItem(itemstack);
                float pow = 0.7f;
                brickEntity.func_234612_a_(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F*pow, 1.0F*pow);
                worldIn.addEntity(brickEntity);
            }

            if (!playerIn.abilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            //playerIn.swingArm(handIn);
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.func_233537_a_(worldIn.isRemote));

        }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item i = event.getItemStack().getItem();
        if(ServerConfigs.cached.WALL_LANTERN_PLACEMENT && isLantern(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.wall_lantern").mergeStyle(TextFormatting.GRAY));
        }
        else if(ServerConfigs.cached.THROWABLE_BRICKS && isBrick(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").mergeStyle(TextFormatting.GRAY));
        }
    }

    /*
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent event) {

        MatrixStack matrixStack = event.getMatrixStack();
        PlayerEntity player = event.getPlayer();
        PlayerRenderer renderer = event.getRenderer();
        IRenderTypeBuffer buffer = event.getBuffers();
        float partialTicks = event.getPartialRenderTick();
        int light = event.getLight();

        //renderer.getEntityModel().rightArmPose= BipedModel.ArmPose.CROSSBOW_CHARGE;
        renderer.getEntityModel().bipedLeftLeg.showModel=false;
        if (player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof Flute) {
            renderer.getEntityModel().bipedLeftArm.rotateAngleZ = 20;


     }

    }*/
}
