package net.mehvahdjukaar.supplementaries.integration;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.integration.cctweaked.CCPlugin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CompatHandler {

    public static final boolean quark;
    public static final boolean deco_blocks;
    public static final boolean configured;
    public static final boolean create;
    public static final boolean torchslab;
    public static final boolean curios;
    public static final boolean farmers_delight;
    public static final boolean infernalexp;
    public static final boolean inspirations;
    public static final boolean framedblocks;
    public static final boolean rgblib;
    public static final boolean endergetic;
    public static final boolean buzzier_bees;
    public static final boolean autumnity;
    public static final boolean deco_blocks_abnormals;
    public static final boolean much_more_mod_compat;
    public static final boolean flywheel;
    public static final boolean repurposed_structures;
    public static final boolean tetra;
    public static final boolean pokecube_legends;
    public static final boolean pokecube;
    public static final boolean dynamictrees;
    public static final boolean moreminecarts;
    public static final boolean habitat;
    public static final boolean simplefarming;
    public static final boolean atmospheric;
    public static final boolean enchantedbookredesign;
    public static final boolean computercraft;
    public static final boolean customvillagertrades;
    public static final boolean nethersdelight;
    public static final boolean doubledoors;
    public static final boolean malum;
    public static final boolean botania;
    public static final boolean mapatlas;
    public static final boolean waystones;
    public static final boolean overweight_farming;
    public static final boolean snowyspirit;

    static {
        quark = isLoaded("quark");
        deco_blocks = isLoaded("decorative_blocks");
        configured = isLoaded("configured");
        create = isLoaded("create");
        torchslab = isLoaded("torchslabmod");
        curios = isLoaded("curios");
        farmers_delight = isLoaded("farmersdelight");
        infernalexp = isLoaded("infernalexp");
        inspirations = isLoaded("inspirations");
        framedblocks = isLoaded("framedblocks");
        rgblib = isLoaded("rgblib");
        endergetic = isLoaded("endergetic");
        deco_blocks_abnormals = isLoaded("decorative_blocks_abnormals");
        much_more_mod_compat = isLoaded("muchmoremodcompat");
        autumnity = isLoaded("autumnity");
        buzzier_bees = isLoaded("buzzier_bees");
        flywheel = isLoaded("flywheel");
        repurposed_structures = isLoaded("repurposed_structures");
        tetra = isLoaded("tetra");
        pokecube_legends = isLoaded("pokecube_legends");
        pokecube = isLoaded("pokecube");
        dynamictrees = isLoaded("dynamictrees");
        moreminecarts = isLoaded("moreminecarts");
        habitat = isLoaded("habitat");
        simplefarming = isLoaded("simplefarming");
        atmospheric = isLoaded("atmospheric");
        enchantedbookredesign = isLoaded("enchantedbookredesign");
        customvillagertrades = isLoaded("customvillagertrades");
        computercraft = isLoaded("computercraft");
        nethersdelight = isLoaded("nethers_delight");
        doubledoors = isLoaded("doubledoors");
        malum = isLoaded("malum");
        botania = isLoaded("botania");
        mapatlas = isLoaded("map_atlases");
        waystones = isLoaded("waystones");
        overweight_farming = isLoaded("overweight_farming");
        snowyspirit = isLoaded("snowyspirit");
    }

    private static boolean isLoaded(String name) {
        return PlatformHelper.isModLoaded(name);
    }

    public static void init() {
        // if (create) CreatePlugin.initialize();
        if (computercraft) CCPlugin.initialize();

        //var i = ForgeRegistries.ITEMS.getValue(new ResourceLocation("quark:ancient_tome"));
        // if (i != Items.AIR) ((IPlaceableItem) i).addPlaceable(ModRegistry.BOOK_PILE.get());
    }

    public static void registerOptionalStuff() {
        //   if (deco_blocks) DecoBlocksCompatRegistry.registerStuff();
        //   if (farmers_delight) FDCompatRegistry.registerStuff();
        //    if (botania) BotaniaCompatRegistry.registerStuff();
        //if (inspirations) CauldronRecipes.registerStuff();
    }

    //compat methods
    public static boolean isVerticalSlabEnabled() {
        return true;
        //CompatHandler.quark
    }


    public static boolean canRenderBlackboardTooltip() {
        return true;
    }

    public static boolean canRenderQuarkTooltip() {
        return true;
    }

    public static boolean shouldHaveButtonOnRight() {
        return false;
    }

    public static Block DynTreesGetOptionalDynamicSapling(Item item, Level level, BlockPos worldPosition) {
        return null;
    }

    public static boolean interactWithFramedSignPost(SignPostBlockTile tile, Player player, InteractionHand handIn, ItemStack itemstack, Level level, BlockPos pos) {
        return false;
    }

    //TODO: fix when there are multiple keys
    public static KeyLockableTile.KeyStatus isKeyInCurio(Player player, String key) {
        return KeyLockableTile.KeyStatus.NO_KEY;
    }

    //use compat blocks
    public static boolean isQuarkTome(Item i) {
        return false;
    }

    public static InteractionResult FDonCakeInteraction(BlockState state, BlockPos pos, Level level, ItemStack itemstack) {
        return InteractionResult.PASS;
    }

    public static void tryConvertingRopeChandelier(BlockState facingState, LevelAccessor worldIn, BlockPos facingPos) {
    }

    public static boolean canMoveTile(BlockState state) {
        //use quark logic if installed
        return true;
    }

    public static boolean isCCprintedBook(Item item) {
    }

    public static int CCgetPages(ItemStack itemstack) {
    }

    public static String[] CCgetText(ItemStack itemstack) {
    }

    //arent these tagged?
    public static boolean isPalisade(BlockState state) {
    }

    public static VertexConsumer getBookColoredFoil(ItemStack stack, MultiBufferSource buffer) {
        return null;
    }

    public static Block tryGettingFramedBlock(Block targetBlock, Level world, BlockPos blockpos) {
    }

    public static int getSacksInBackpack(ItemStack backpack) {
    }


    public static BlockEntity getQuarkMovingTile(BlockPos pos, Level level) {
    }
}
