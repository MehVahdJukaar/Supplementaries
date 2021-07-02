package net.mehvahdjukaar.supplementaries.events;


import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.PicklePlayer;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.items.BlockHolderItem;
import net.mehvahdjukaar.supplementaries.items.EmptyJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SendLoginMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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

    private static final JarBlockTile DUMMY_JAR_TILE = new JarBlockTile();

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

        BlockState blockstate = world.getBlockState(pos);

        //map markers
        if (i instanceof FilledMapItem && ServerConfigs.cached.MAP_MARKERS){
            Block b = blockstate.getBlock();
            if(b instanceof BedBlock||b.is(Blocks.LODESTONE)||b.is(Blocks.NETHER_PORTAL)||b.is(Blocks.BEACON)||
                    b.is(Blocks.CONDUIT)||b.is(Blocks.RESPAWN_ANCHOR)||b.is(Blocks.END_GATEWAY)||b.is(Blocks.END_PORTAL)){
                if(!world.isClientSide) {
                    MapData data = FilledMapItem.getOrCreateSavedData(stack, world);
                    if (data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(world, pos);
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.sidedSuccess(world.isClientSide));
                return;
            }
        }


        //order matters here
        if (!player.isShiftKeyDown()) {

            //directional cake conversion
            if (ServerConfigs.cached.DIRECTIONAL_CAKE && blockstate == Blocks.CAKE.defaultBlockState() &&
                    !(ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT && i == Items.CAKE)) {
                world.setBlock(pos, Registry.DIRECTIONAL_CAKE.get().defaultBlockState(), 4);
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

        //xp bottling
        if (ServerConfigs.cached.BOTTLE_XP && blockstate.getBlock() instanceof EnchantingTableBlock) {
            ItemStack returnStack = null;

            //prevent accidentally releasing bottles
            if(i == Items.EXPERIENCE_BOTTLE){
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.FAIL);
                return;
            }

            if(player.experienceLevel > 0 || player.isCreative()){
                if (i == Items.GLASS_BOTTLE){
                    returnStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                }
                else if(i instanceof EmptyJarItem || i instanceof JarItem){
                    DUMMY_JAR_TILE.resetHolders();
                    CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
                    if (compoundnbt != null) {
                        DUMMY_JAR_TILE.load(((BlockItem) i).getBlock().defaultBlockState(), compoundnbt);
                    }

                    if (DUMMY_JAR_TILE.isEmpty() && (DUMMY_JAR_TILE.mobHolder.isEmpty()|| DUMMY_JAR_TILE.isPonyJar())) {
                        ItemStack tempStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                        ItemStack temp = DUMMY_JAR_TILE.fluidHolder.interactWithItem(tempStack, null, null);
                        if(temp!=null && temp.getItem() == Items.GLASS_BOTTLE){
                            returnStack = ((JarBlock)((BlockItem) i).getBlock()).getJarItem(DUMMY_JAR_TILE);
                        }
                    }
                }


                if(returnStack!=null){
                    player.hurt(CommonUtil.BOTTLING_DAMAGE, ServerConfigs.cached.BOTTLING_COST);
                    CommonUtil.swapItem(player, hand, returnStack);

                    if (!player.isCreative())
                        player.giveExperiencePoints(-CommonUtil.bottleToXP(1,world.random));

                    if (world.isClientSide) {
                        Minecraft.getInstance().particleEngine.createTrackingEmitter(player, Registry.BOTTLING_XP_PARTICLE.get(), 1);
                    }
                    world.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 1, 1);
                    event.setCanceled(true);
                    event.setCancellationResult(ActionResultType.sidedSuccess(world.isClientSide));
                    return;
                }
            }
            return;
        }

        //block overrides
        if(player.abilities.mayBuild) {

            ActionResultType result = ActionResultType.PASS;

            //sticks
            if (ServerConfigs.cached.PLACEABLE_STICKS && i == Items.STICK) {
                result = paceBlockOverride(Registry.STICK_BLOCK_ITEM.get(), player, hand, null, pos, dir, world);
            }
            else if (ServerConfigs.cached.PLACEABLE_RODS && i == Items.BLAZE_ROD) {
                result = paceBlockOverride(Registry.BLAZE_ROD_ITEM.get(), player, hand, null, pos, dir, world);
            }
            else if(i instanceof BlockItem) {
                BlockItem bi = (BlockItem) i;
                //wall lantern
                if (ServerConfigs.cached.WALL_LANTERN_PLACEMENT && CommonUtil.isLantern(bi)) {
                    if (CompatHandler.torchslab) {
                        double y = event.getHitVec().getLocation().y() % 1;
                        if (y < 0.5) return;
                    }
                    result = paceBlockOverride(Registry.WALL_LANTERN_ITEM.get(), player, hand, bi, pos, dir, world);
                }
                //hanging pot
                else if (ServerConfigs.cached.HANGING_POT_PLACEMENT && CommonUtil.isPot(bi)) {
                    result = paceBlockOverride(Registry.HANGING_FLOWER_POT_ITEM.get(), player, hand, bi, pos, dir, world);
                }
                //double cake
                else if (CommonUtil.isCake(bi)) {
                    if (ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT)
                        result = placeDoubleCake(player, stack, pos, world);
                    if (!result.consumesAction() && ServerConfigs.cached.DIRECTIONAL_CAKE)
                        result = paceBlockOverride(Registry.DIRECTIONAL_CAKE_ITEM.get(), player, hand, bi, pos, dir, world);
                }
                else if (ServerConfigs.cached.CEILING_BANNERS && bi instanceof BannerItem && dir==Direction.DOWN){
                    result = paceBlockOverride(Registry.CEILING_BANNERS_ITEMS.get(((BannerItem) bi).getColor()).get(), player, hand, bi, pos, dir, world);
                }
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
    //raked gravel
    @SubscribeEvent
    public static void onHoeUsed(UseHoeEvent event) {
        ItemUseContext context = event.getContext();
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (ServerConfigs.cached.RAKED_GRAVEL){
            if(world.getBlockState(pos).is(Blocks.GRAVEL)){
                BlockState raked = Registry.RAKED_GRAVEL.get().defaultBlockState();
                if(raked.canSurvive(world,pos)){
                    world.setBlock(pos, RakedGravelBlock.getConnectedState(raked,world,pos,context.getHorizontalDirection()),11);
                    world.playSound(context.getPlayer(), pos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (CompatHandler.quark && event.getObject().getItem() == Registry.SACK_ITEM.get()) {
            QuarkPlugin.attachSackDropIn(event);
        }

    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                    new SendLoginMessagePacket());
        }
        catch (Exception exception){
            Supplementaries.LOGGER.warn("failed to end login message: "+ exception);
        }
        //send in pickles
        PicklePlayer.PickleData.onPlayerLogin(event.getPlayer());

    }

    @SubscribeEvent
    public static void serverAboutToStart(final FMLServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        StatueBlockTile.profileCache = server.getProfileCache();
        StatueBlockTile.sessionService = server.getSessionService();
        //PlayerProfileCache.setOnlineMode(server.isServerInOnlineMode());
    }

}
