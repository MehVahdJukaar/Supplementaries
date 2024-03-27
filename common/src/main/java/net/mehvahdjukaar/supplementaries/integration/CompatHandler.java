package net.mehvahdjukaar.supplementaries.integration;


import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class CompatHandler {

    public static final boolean AMENDMENTS = isLoaded("amendments");
    public static final boolean QUARK = isLoaded("quark");
    public static final boolean JEI = isLoaded("jei");
    public static final boolean REI = isLoaded("roughlyenoughitems");
    public static final boolean EMI = isLoaded("emi");
    public static final boolean DECO_BLOCKS = isLoaded("decorative_blocks");
    public static final boolean GOATED = isLoaded("goated");
    public static final boolean IMMEDIATELY_FAST = isLoaded("immediatelyfast");
    public static final boolean CONFIGURED = isLoaded("configured");
    public static final boolean OREGANIZED = isLoaded("oreganized");
    public static final boolean CREATE = isLoaded("create");
    public static final boolean TORCHSLAB = isLoaded("torchslabmod");
    public static final boolean CURIOS = isLoaded("curios");
    public static final boolean TRINKETS = isLoaded("trinkets");
    public static final boolean FARMERS_DELIGHT;
    public static final boolean INFERNALEXP = isLoaded("infernalexp");
    public static final boolean INSPIRATIONS = isLoaded("inspirations");
    public static final boolean FRAMEDBLOCKS = isLoaded("framedblocks");
    public static final boolean RGBLIB = isLoaded("rgblib");
    public static final boolean ENDERGETIC = isLoaded("endergetic");
    public static final boolean DECO_BLOCKS_ABNORMALS = isLoaded("decorative_blocks_abnormals");
    public static final boolean MUCH_MORE_MOD_COMPAT = isLoaded("muchmoremodcompat");
    public static final boolean AUTUMNITY = isLoaded("autumnity");
    public static final boolean BUZZIER_BEES = isLoaded("buzzier_bees");
    public static final boolean FLYWHEEL = isLoaded("flywheel");
    public static final boolean REPURPOSED_STRUCTURES = isLoaded("repurposed_structures");
    public static final boolean TETRA = isLoaded("tetra");
    public static final boolean POKECUBE_LEGENDS = isLoaded("pokecube_legends");
    public static final boolean POKECUBE = isLoaded("pokecube");
    public static final boolean DYNAMICTREES = isLoaded("dynamictrees");
    public static final boolean MOREMINECARTS = isLoaded("moreminecarts");
    public static final boolean HABITAT = isLoaded("habitat");
    public static final boolean SIMPLEFARMING = isLoaded("simplefarming");
    public static final boolean ATMOSPHERIC = isLoaded("atmospheric");
    public static final boolean ENCHANTEDBOOKREDESIGN = isLoaded("enchantedbookredesign");
    public static final boolean CUSTOMVILLAGERTRADES = isLoaded("customvillagertrades");
    public static final boolean COMPUTERCRAFT = isLoaded("computercraft");
    public static final boolean NETHERSDELIGHT = isLoaded("nethers_delight");
    public static final boolean DOUBLEDOORS = isLoaded("doubledoors");
    public static final boolean MALUM = isLoaded("malum");
    public static final boolean BOTANIA = isLoaded("botania");
    public static final boolean MAPATLAS = isLoaded("map_atlases");
    public static final boolean SUPPSQUARED = isLoaded("suppsquared");
    public static final boolean WAYSTONES = PlatHelper.getPlatform().isForge() ? isLoaded("waystones") : isLoaded("fwaystones");
    public static final boolean OVERWEIGHT_FARMING = isLoaded("overweight_farming");
    public static final boolean SNOWYSPIRIT = isLoaded("snowyspirit");
    public static final boolean HAUNTEDHARVEST = isLoaded("hauntedharvest");
    public static final boolean CLOTH_CONFIG = isLoaded("cloth_config");
    public static final boolean FLAN = isLoaded("flan");
    public static final boolean BREEZY = isLoaded("breezy");
    public static final boolean SHIMMER = isLoaded("shimmer");
    public static final boolean BUMBLEZONE = isLoaded("the_bumblezone");
    public static final boolean CAVE_ENHANCEMENTS = isLoaded("cave_enhancements");
    public static final boolean CUSTOM_PLAYER_MODELS = isLoaded("cpm");
    public static final boolean FARMERS_RESPRITE = isLoaded("farmersrespite");
    public static final boolean ARCHITECTS_PALETTE = isLoaded("architects_palette");
    public static final boolean OPTIFINE;

    static {
        boolean of = false;
        if (PlatHelper.getPhysicalSide().isClient()) {
            try {
                Class.forName("net.optifine.Config");
                of = true;
            } catch (ClassNotFoundException ignored) {
            }
        }

        OPTIFINE = of;

        boolean fd = false;
        if (isLoaded("farmersdelight")) {
            try {
                Class.forName("vectorwing.farmersdelight.FarmersDelight");
                fd = true;
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Farmers Delight Refabricated is not installed. Disabling Farmers Delight Module");
            }
        }
        FARMERS_DELIGHT = fd;
    }

    private static boolean isLoaded(String name) {
        return PlatHelper.isModLoaded(name);
    }

    public static void setup() {
        if (CREATE) CreateCompat.setup();
        if (COMPUTERCRAFT) CCCompat.setup();
    }

    public static void initOptionalRegistries() {
        if (FARMERS_DELIGHT) FarmersDelightCompat.init();
        if (DECO_BLOCKS) DecoBlocksCompat.init();
        if (QUARK) QuarkCompat.init();
        if (ENDERGETIC) EndergeticCompat.init();
        if (INFERNALEXP) InfernalExpCompat.init();
        if (ARCHITECTS_PALETTE) ArchitectsPalCompat.init();
        //if (inspirations) CauldronRecipes.registerStuff();
    }

    public static void addItemsToTabs(RegHelper.ItemToTabEvent event) {
    }


    public static Block DynTreesGetOptionalDynamicSapling(Item item, Level level, BlockPos worldPosition) {
        return null;
    }

    public static KeyLockableTile.KeyStatus getKeyFromModsSlots(Player player, String key) {
        IKeyLockable.KeyStatus status = IKeyLockable.KeyStatus.NO_KEY;
        if (CompatHandler.CURIOS) {
            status = CuriosCompat.getKey(player, key);
            if (status != IKeyLockable.KeyStatus.NO_KEY) return status;
        }
        if (CompatHandler.TRINKETS) {
            status = TrinketsCompat.getKey(player, key);
            if (status != IKeyLockable.KeyStatus.NO_KEY) return status;
        }
        return status;
    }

    @NotNull
    public static ItemStack getQuiverFromModsSlots(Player player) {
        ItemStack stack = ItemStack.EMPTY;
        if (CompatHandler.CURIOS) {
            stack = CuriosCompat.getQuiver(player);
            if (!stack.isEmpty()) return stack;
        }
        if (CompatHandler.TRINKETS) {
            stack = TrinketsCompat.getQuiver(player);
            if (!stack.isEmpty()) return stack;
        }
        return stack;
    }

}
