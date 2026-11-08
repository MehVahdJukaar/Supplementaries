package net.mehvahdjukaar.supplementaries.integration;


import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceWallBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WeatheringSconceBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WeatheringSconceWallBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regBlock;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regItem;

public class VanillaBackportCompat {

    private static final Map<WeatherState, CopperSconce> COPPER_SCONCES = new EnumMap<>(WeatherState.class);
    private static final Map<WeatherState, CopperSconce> WAXED_COPPER_SCONCES = new EnumMap<>(WeatherState.class);

    static {
        for (WeatherState age : WeatherState.values()) {
            COPPER_SCONCES.put(age, regCopperSconce(age, false));
        }
        for (WeatherState age : WeatherState.values()) {
            WAXED_COPPER_SCONCES.put(age, regCopperSconce(age, true));
        }
    }

    public static void init() {
        COPPER_SCONCES.values().forEach(s -> ModRegistry.SCONCES.add(s.item));
        WAXED_COPPER_SCONCES.values().forEach(s -> ModRegistry.SCONCES.add(s.item));
    }

    private static CopperSconce regCopperSconce(WeatherState age, boolean waxed) {
        String prefix = (waxed ? "waxed_" : "") + (age == WeatherState.UNAFFECTED ? "" : age.getSerializedName() + "_");
        String name = prefix + "copper_sconce";

        Supplier<Block> standing = regBlock(name, () -> {
            var p = BlockBehaviour.Properties.ofFullCopy(ModRegistry.SCONCE.get());
            if (waxed) {
                return new SconceBlock(p, 14, CompatObjects.COPPER_FLAME);
            }
            else return new WeatheringSconceBlock(age, p, 14, CompatObjects.COPPER_FLAME);
        });
        Supplier<Block> wall = regBlock(prefix + "copper_wall_sconce", () -> {
            var p = BlockBehaviour.Properties.ofFullCopy(standing.get()).dropsLike(standing.get());
            if (waxed){
                return new SconceWallBlock(p, CompatObjects.COPPER_FLAME);
            }
            else return new WeatheringSconceWallBlock(age, p, CompatObjects.COPPER_FLAME);
        });
        Supplier<Item> item = regItem(name, () -> new StandingAndWallBlockItem(
                standing.get(), wall.get(), new Item.Properties(), Direction.DOWN));
        return new CopperSconce(standing, wall, item);
    }

    public static void setupClient() {
        for (var s : COPPER_SCONCES.values()) {
            ClientHelper.registerRenderType(s.standing.get(), RenderType.cutout());
            ClientHelper.registerRenderType(s.wall.get(), RenderType.cutout());
        }
        for (CopperSconce s : WAXED_COPPER_SCONCES.values()) {
            ClientHelper.registerRenderType(s.standing.get(), RenderType.cutout());
            ClientHelper.registerRenderType(s.wall.get(), RenderType.cutout());
        }
    }

    //only add when mod is on. conditions there dont work well (on fabricc)
    public static void addCopperDataMaps(ResourceSink sink) {
        JsonObject oxidizables = new JsonObject();
        JsonObject waxables = new JsonObject();
        WeatherState[] ages = WeatherState.values();
        for (int i = 0; i < ages.length; i++) {
            CopperSconce current = COPPER_SCONCES.get(ages[i]);
            CopperSconce waxed = WAXED_COPPER_SCONCES.get(ages[i]);
            addEntry(waxables, current.standing, "waxed", waxed.standing);
            addEntry(waxables, current.wall, "waxed", waxed.wall);

            if (i + 1 < ages.length) {
                CopperSconce next = COPPER_SCONCES.get(ages[i + 1]);
                addEntry(oxidizables, current.standing, "next_oxidation_stage", next.standing);
                addEntry(oxidizables, current.wall, "next_oxidation_stage", next.wall);
            }
        }
        sink.addJson(ResourceLocation.fromNamespaceAndPath("neoforge", "data_maps/block/oxidizables.json"),
                dataMap(oxidizables), ResType.GENERIC);
        sink.addJson(ResourceLocation.fromNamespaceAndPath("neoforge", "data_maps/block/waxables.json"),
                dataMap(waxables), ResType.GENERIC);
    }

    private static void addEntry(JsonObject values, Supplier<Block> from, String key, Supplier<Block> to) {
        JsonObject entry = new JsonObject();
        entry.addProperty(key, BuiltInRegistries.BLOCK.getKey(to.get()).toString());
        values.add(BuiltInRegistries.BLOCK.getKey(from.get()).toString(), entry);
    }

    private static JsonObject dataMap(JsonObject values) {
        JsonObject json = new JsonObject();
        json.addProperty("moonlight_parse_on_fabric", true);
        json.add("values", values);
        return json;
    }

    public static void swapDeepslateLampTexture(ResourceManager manager, ResourceSink sink) {
        var t = ResType.BLOCK_TEXTURES.getPath(Supplementaries.res("deepslate_lamp_green"));
        sink.copyResource(manager, t, ResType.BLOCK_TEXTURES.getPath(Supplementaries.res("deepslate_lamp")), false);
        sink.copyResource(manager, t, ResType.BLOCK_TEXTURES.getPath(Supplementaries.res("deepslate_lamp_top")), false);
    }

    private record CopperSconce(Supplier<Block> standing, Supplier<Block> wall, Supplier<Item> item) {
    }

}
