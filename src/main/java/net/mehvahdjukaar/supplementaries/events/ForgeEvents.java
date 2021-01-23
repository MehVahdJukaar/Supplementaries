package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.items.BlockHolderItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.mehvahdjukaar.supplementaries.world.data.GlobeData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {







    private static ActionResultType paceBlockOverride(Item itemOverride, PlayerEntity player, Hand hand,
                                                      BlockItem heldItem, BlockPos pos, Direction dir, World world ){

            if (dir != null) {
                //try interact with block behind
                BlockState blockstate = world.getBlockState(pos);
                BlockRayTraceResult raytrace = new BlockRayTraceResult(
                        new Vector3d(pos.getX(), pos.getY(), pos.getZ()), dir, pos, false);

                if (!player.isSneaking()) {
                    ActionResultType activationResult = blockstate.onBlockActivated(world, player, hand, raytrace);
                    if(activationResult.isSuccessOrConsume())return activationResult;
                }

                //place block
                BlockItemUseContext ctx = new BlockItemUseContext(
                        new ItemUseContext(player, hand, raytrace));
                if(itemOverride instanceof BlockHolderItem) {
                    return ((BlockHolderItem) itemOverride).tryPlace(ctx, heldItem.getBlock());
                }
                else if(itemOverride instanceof BlockItem) {
                    return ((BlockItem) itemOverride).tryPlace(ctx);
                }

        }
        return  ActionResultType.PASS;
    }













    //TODO: split into different classes

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
        try {
            return ((Tags.Items.INGOTS_BRICK != null && i.isIn(Tags.Items.INGOTS_BRICK))
                    || (Tags.Items.INGOTS_NETHER_BRICK != null && i.isIn(Tags.Items.INGOTS_NETHER_BRICK))||
                    ServerConfigs.cached.BRICKS_LIST.contains(i.getRegistryName().toString()));
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isPot(Item i){
        if(i instanceof BlockItem){
            Block b =  ((BlockItem) i).getBlock();
            //String namespace = b.getRegistryName().getNamespace();
            return ((b instanceof FlowerPotBlock));
        }
        return false;
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
        Direction dir = event.getFace();
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        //bell chains
        if(stack.isEmpty() && hand==Hand.MAIN_HAND){
            if(!ServerConfigs.cached.BELL_CHAIN)return;
            if(findConnectedBell(world,pos,player,0)){
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.func_233537_a_(world.isRemote));
            }
            return;
        }

        //block overrides
        if(player.abilities.allowEdit && i instanceof BlockItem) {
            BlockItem bi = (BlockItem) i;
            ActionResultType result = ActionResultType.PASS;
            if (ServerConfigs.cached.WALL_LANTERN_PLACEMENT && isLantern(bi)) {
                result = paceBlockOverride(Registry.WALL_LANTERN_ITEM, player, hand, bi, pos, dir, world);
            }
            else if (ServerConfigs.cached.HANGING_POT_PLACEMENT && isPot(bi)) {
                result = paceBlockOverride(Registry.HANGING_FLOWER_POT_ITEM, player, hand, bi, pos, dir, world);
            }

            if (result.isSuccessOrConsume()) {
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.RIGHT_CLICK_BLOCK_WITH_ITEM.test((ServerPlayerEntity) player, pos, stack);
                }
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }

    //bricks
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(!ServerConfigs.cached.THROWABLE_BRICKS_ENABLED)return;
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
        if((event.getPlayer()==null)||(event.getPlayer().world==null))return;
        Item i = event.getItemStack().getItem();
        if(ServerConfigs.cached.WALL_LANTERN_PLACEMENT && isLantern(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.wall_lantern").mergeStyle(TextFormatting.GRAY));
        }
        else if(ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && isBrick(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").mergeStyle(TextFormatting.GRAY));
        }
        else if(ServerConfigs.cached.HANGING_POT_PLACEMENT && isPot(i)){
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.hanging_pot").mergeStyle(TextFormatting.GRAY));
        }
    }





    //enderman hold block in rain
/*
    @SubscribeEvent
    public static void onRenderEnderman(RenderLivingEvent<EndermanEntity, EndermanModel<EndermanEntity>> event) {
        if(event.getEntity()instanceof EndermanEntity){
            LivingRenderer<EndermanEntity, EndermanModel<EndermanEntity>> renderer = event.getRenderer();
            if(renderer instanceof EndermanRenderer) {
                MatrixStack matrixStack = event.getMatrixStack();
                matrixStack.push();

                //renderer.getEntityModel().bipedLeftArm.showModel=false;

                //event.getRenderer().getEntityModel().bipedLeftArm.rotateAngleX=180;


                event.getRenderer().getEntityModel().bipedLeftArm.showModel=true;
                //bipedRightArm.rotateAngleX=100;
                int i = getPackedOverlay(event.getEntity(), 0);
                //event.getRenderer().getEntityModel().bipedLeftArm.render(event.getMatrixStack(),event.getBuffers().getBuffer(RenderType.getEntityCutout(new ResourceLocation("textures/entity/enderman/enderman.png"))), event.getLight(),i);
                event.getRenderer().getEntityModel().bipedLeftArm.showModel=false;
                matrixStack.pop();
            }
        }
    }*/
    /*
    @SubscribeEvent
    public static void onRenderEnderman(PlayerInteractEvent.EntityInteractSpecific event) {

        Entity e = event.getTarget();
        if(e instanceof MobEntity && event.getItemStack().getItem() instanceof CompassItem){
            ((MobEntity) e).setHomePosAndDistance(new BlockPos(0,63,0),100);
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
        }
    }*/


}
