package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.items.BlockHolderItem;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SendLoginMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.PacketDistributor;

//@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    //TODO: split into different classes

    private static ActionResultType paceBlockOverride(Item itemOverride, PlayerEntity player, Hand hand,
                                                      BlockItem heldItem, BlockPos pos, Direction dir, World world ){
            if (dir != null) {
                //try interact with block behind
                BlockState blockstate = world.getBlockState(pos);
                BlockRayTraceResult raytrace = new BlockRayTraceResult(
                        new Vector3d(pos.getX(), pos.getY(), pos.getZ()), dir, pos, false);

                if (!player.isShiftKeyDown()) {
                    ActionResultType activationResult = blockstate.use(world, player, hand, raytrace);
                    if(activationResult.consumesAction())return activationResult;
                }

                //place block


                BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(player, hand, raytrace));
                if(itemOverride instanceof BlockHolderItem) {
                    return ((BlockHolderItem) itemOverride).tryPlace(ctx, heldItem.getBlock());
                }
                else if(itemOverride instanceof BlockItem) {
                    return ((BlockItem) itemOverride).place(ctx);
                }


        }
        return  ActionResultType.PASS;
    }

    public static ActionResultType placeDoubleCake(PlayerEntity player, ItemStack stack, BlockPos pos, World world ){
        BlockState state1 = world.getBlockState(pos);
        boolean d = state1.getBlock()==Registry.DIRECTIONAL_CAKE.get();
        if((d && state1.getValue(DirectionalCakeBlock.BITES)==0) || state1==Blocks.CAKE.defaultBlockState()) {
            BlockState state = Registry.DOUBLE_CAKE.get().defaultBlockState()
                    .setValue(DoubleCakeBlock.FACING,d?state1.getValue(DoubleCakeBlock.FACING):Direction.WEST)
                    .setValue(DoubleCakeBlock.WATERLOGGED, world.getFluidState(pos).getType()==Fluids.WATER);
            if (!world.setBlock(pos, state, 3)) {
                return ActionResultType.FAIL;
            }
            if (player instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
            }
            SoundType soundtype = state.getSoundType(world, pos, player);
            world.playSound(player, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            if (player == null || !player.abilities.instabuild) {
                stack.shrink(1);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }


    private static boolean findConnectedBell(World world, BlockPos pos, PlayerEntity player, int it){
        if(it>ServerConfigs.cached.BELL_CHAIN_LENGTH)return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if(b instanceof ChainBlock){
            return findConnectedBell(world,pos.above(),player,it+1);
        }
        else if(b instanceof BellBlock && it !=0){
            boolean success = ((BellBlock) b).attemptToRing(world, pos, state.getValue(BellBlock.FACING).getClockWise());
            if (success && player != null) {
                player.awardStat(Stats.BELL_RING);
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if(player.isSpectator())return;
        Hand hand = event.getHand();
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        Direction dir = event.getFace();
        World world = event.getWorld();
        BlockPos pos = event.getPos();


        //order matters here
        if (!player.isShiftKeyDown()) {
            //directional cake conversion
            if (ServerConfigs.cached.DIRECTIONAL_CAKE && world.getBlockState(pos) == Blocks.CAKE.defaultBlockState() &&
                    !(ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT && i == Items.CAKE)) {
                world.setBlock(pos, Registry.DIRECTIONAL_CAKE.get().defaultBlockState(), 4);
                BlockState blockstate = world.getBlockState(pos);
                BlockRayTraceResult raytrace = new BlockRayTraceResult(
                        new Vector3d(pos.getX(), pos.getY(), pos.getZ()), dir, pos, false);
                event.setCancellationResult(blockstate.use(world, player, hand, raytrace));
                event.setCanceled(true);
                return;
            }

            //bell chains
            if (stack.isEmpty() && hand == Hand.MAIN_HAND) {
                if (ServerConfigs.cached.BELL_CHAIN) {
                    if (findConnectedBell(world, pos, player, 0)) {
                        event.setCanceled(true);
                        event.setCancellationResult(ActionResultType.sidedSuccess(world.isClientSide));
                    }
                    return;
                }
            }
        }

        //block overrides
        if(player.abilities.mayBuild && i instanceof BlockItem) {
            BlockItem bi = (BlockItem) i;
            ActionResultType result = ActionResultType.PASS;
            if (ServerConfigs.cached.WALL_LANTERN_PLACEMENT && CommonUtil.isLantern(bi)) {
                if(ModList.get().isLoaded("torchslabmod")){
                    double y = event.getHitVec().getLocation().y()%1;
                    if(y<0.5)return;
                }
                result = paceBlockOverride(Registry.WALL_LANTERN_ITEM.get(), player, hand, bi, pos, dir, world);
            }
            else if (ServerConfigs.cached.HANGING_POT_PLACEMENT && CommonUtil.isPot(bi)) {
                result = paceBlockOverride(Registry.HANGING_FLOWER_POT_ITEM.get(), player, hand, bi, pos, dir, world);
            }
            else if (CommonUtil.isCake(bi)) {
                if(ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT)
                    result = placeDoubleCake(player, stack, pos, world);
                if(!result.consumesAction() && ServerConfigs.cached.DIRECTIONAL_CAKE)
                    result = paceBlockOverride(Registry.DIRECTIONAL_CAKE_ITEM.get(), player, hand, bi, pos, dir, world);
            }

            if (result.consumesAction()) {
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
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
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        Item i = itemstack.getItem();
        if(CommonUtil.isBrick(i)) {
            World worldIn = event.getWorld();
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (playerIn.getRandom().nextFloat() * 0.4F + 0.8F ));
            if (!worldIn.isClientSide) {
                ThrowableBrickEntity brickEntity = new ThrowableBrickEntity(worldIn, playerIn);
                brickEntity.setItem(itemstack);
                float pow = 0.7f;
                brickEntity.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.5F*pow, 1.0F*pow);
                worldIn.addFreshEntity(brickEntity);
            }

            if (!playerIn.abilities.instabuild) {
                itemstack.shrink(1);
            }

            //playerIn.swingArm(handIn);
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.sidedSuccess(worldIn.isClientSide));
        }

    }



    //TODO: maybe use player logged in and send packet
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new SendLoginMessagePacket());

    }

}
