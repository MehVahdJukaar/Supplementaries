package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.mixins.ProjectileWeaponItemMixin;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//to whoever is reading I'm sorry :/
public class StuffToRemove {

    public static final List<String> COMMAND_MOBS = new ArrayList<>();
    public static final List<DataDefinedCatchableMob> DEFAULT_CONFIG = new ArrayList<>();


    private static DataDefinedCatchableMob addFish(String id) {
        return addDef(id, 0, 0.125f, 0, ++fishIndex);
    }

    private static DataDefinedCatchableMob addFish(String id, int fishIndex) {
        return addDef(id, 0, 0.125f, 0, fishIndex);
    }

    private static DataDefinedCatchableMob addDef(String id, float h, float w, int l, int f) {
        return new DataDefinedCatchableMob(List.of(new ResourceLocation(id)),w,h,l, Optional.empty(),
                f, BuiltinAnimation.Type.NONE, DataDefinedCatchableMob.TickMode.NONE, Optional.empty(),Optional.empty());
    }

    private static DataDefinedCatchableMob addDef(String id, float h, float w, int l, BuiltinAnimation.Type c) {
        return new DataDefinedCatchableMob(List.of(new ResourceLocation(id)),w,h,l, Optional.empty(),
                0, c, DataDefinedCatchableMob.TickMode.NONE, Optional.empty(),Optional.empty());
    }

    private static DataDefinedCatchableMob addDef(String id, float h, float w, int l) {
        return addDef(id, h, w, l, 0);
    }

    private static DataDefinedCatchableMob addDef(String id, float h, float w) {
        return addDef(id, h, w, 0, 0);
    }

    private static int fishIndex = 0;

    static {
        //1=default fish

        DEFAULT_CONFIG.add(addDef("minecraft:bee", 0.3125f, 0f));
        DEFAULT_CONFIG.add(addDef("minecraft:vex", 0, 0.125f, 0, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:silverfish", 0, 0.25f));
        DEFAULT_CONFIG.add(addDef("minecraft:chicken", 0.25f, 0.3125f, 0, BuiltinAnimation.Type.BUILTIN));
        DEFAULT_CONFIG.add(addDef("minecraft:endermite", 0, 0, 5, BuiltinAnimation.Type.BUILTIN));
        DEFAULT_CONFIG.add(addDef("minecraft:fox", 0, 0.2f));
        DEFAULT_CONFIG.add(addDef("minecraft:squid", 0.25f, 0.25f, 0, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:glow_squid", 0.25f, 0.25f, 3, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("ecologics:firefly", 0, 0, 9, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("minecraft:parrot", 0, 0, 0, BuiltinAnimation.Type.BUILTIN));
        DEFAULT_CONFIG.add(addDef("minecraft:slime", 0, 0, 0, BuiltinAnimation.Type.BUILTIN));
        DEFAULT_CONFIG.add(addDef("minecraft:rabbit", 0, 0, 0, BuiltinAnimation.Type.BUILTIN));

        DEFAULT_CONFIG.add(addDef("druidcraft:lunar_moth", 0.375f, 0.1375f, 10, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("iceandfire:pixie", 0, 0, 10));

        DEFAULT_CONFIG.add(addDef("feywild:winter_pixie", 0.125f, 0f, 8, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:summer_pixie", 0.125f, 0f, 8, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:spring_pixie", 0.125f, 0f, 8, BuiltinAnimation.Type.FLOATING));
        DEFAULT_CONFIG.add(addDef("feywild:autumn_pixie", 0.25f, 0f, 8, BuiltinAnimation.Type.FLOATING));


        DEFAULT_CONFIG.add(addFish("minecraft:fish"));
        DEFAULT_CONFIG.add(addFish("minecraft:tropical_fish"));
        DEFAULT_CONFIG.add(addFish("minecraft:salmon"));
        DEFAULT_CONFIG.add(addFish("minecraft:cod"));
        DEFAULT_CONFIG.add(addFish("minecraft:pufferfish"));
        DEFAULT_CONFIG.add(addFish("minecraft:axolotl"));
        DEFAULT_CONFIG.add(addFish("finsandtails:pea_wee"));
        DEFAULT_CONFIG.add(addFish("finsandtails:wee_wee"));
        DEFAULT_CONFIG.add(addFish("finsandtails:vibra_wee"));
        DEFAULT_CONFIG.add(addFish("finsandtails:blu_wee"));
        DEFAULT_CONFIG.add(addFish("finsandtails:ornate_bugfish"));
        DEFAULT_CONFIG.add(addFish("finsandtails:spindly_gem_crab"));
        DEFAULT_CONFIG.add(addFish("finsandtails:phantom_nudibranch"));
        DEFAULT_CONFIG.add(addFish("finsandtails:high_finned_blue"));
        DEFAULT_CONFIG.add(addFish("finsandtails:teal_arrowfish"));
        DEFAULT_CONFIG.add(addFish("finsandtails:midnight_squid"));
        DEFAULT_CONFIG.add(addFish("finsandtails:banded_redback_shrimp"));
        DEFAULT_CONFIG.add(addFish("finsandtails:flatback_sucker"));
        DEFAULT_CONFIG.add(addFish("finsandtails:swamp_mucker"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:lionfish"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:nautilus"));
        DEFAULT_CONFIG.add(addFish("upgrade_aquatic:pike"));
        DEFAULT_CONFIG.add(addFish("alexsmobs:stradpole"));
        DEFAULT_CONFIG.add(addFish("alexsmobs:blobfish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:flashlight_fish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:elephantnose_fish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:black_diamond_stingray"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:humphead_parrotfish"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:tomato_frog"));
        DEFAULT_CONFIG.add(addFish("unnamedanimalmod:pacman_frog"));
        DEFAULT_CONFIG.add(addFish("bettas:betta_fish"));
        DEFAULT_CONFIG.add(addFish("pogfish:pogfish"));
        DEFAULT_CONFIG.add(addFish("undergarden:gwibling"));
        DEFAULT_CONFIG.add(addFish("environmental:koi"));
        DEFAULT_CONFIG.add(addFish("betterendforge:end_fish"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:flying_fish"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:nautilus"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:eel_freshwater"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:eel_saltwater"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:lamprey"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:piranha"));
        DEFAULT_CONFIG.add(addFish("betteranimalsplus:barracuda"));
        DEFAULT_CONFIG.add(addFish("rediscovered:fish", 1));
        DEFAULT_CONFIG.add(addFish("biomemakeover:glowfish"));
        DEFAULT_CONFIG.add(addFish("biomemakeover:tadpoles"));
        //DEFAULT_VALUES.add(addDef("----"));


    }


    public static void generateStuff() {
        File folder = PlatformHelper.getGamePath().resolve("test_cap").toFile();

        if (!folder.exists()) {
            folder.mkdir();
        }

DEFAULT_CONFIG.add(new DataDefinedCatchableMob(List.of(new ResourceLocation("pig")),0.2f, 0.4f,
        3, Optional.of(new DataDefinedCatchableMob.CaptureSettings(new DataDefinedCatchableMob.CatchMode(true,false),
        new DataDefinedCatchableMob.CatchMode(true, true))),0,BuiltinAnimation.Type.FLOATING,
        DataDefinedCatchableMob.TickMode.CLIENT,Optional.empty(),
        Optional.of(new DataDefinedCatchableMob.LootParam(new ResourceLocation("test_loot"),0.01f))));

        for(var v : DEFAULT_CONFIG){
            try {

                File exportPath = new File(folder, v.getOwners().get(0).toString().replace(":","_")+ ".json");
                try (FileWriter writer = new FileWriter(exportPath)) {
                    var j = DataDefinedCatchableMob.CODEC.encodeStart(JsonOps.INSTANCE, v);
                    JsonWriter w = new JsonWriter(writer);

                 CapturedMobHandler.GSON.toJson(sortJson(j.result().get().getAsJsonObject()), writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static JsonObject sortJson(JsonObject jsonObject) {
        try {
            Map<String, JsonElement> joToMap = new TreeMap<>();
            jsonObject.entrySet().forEach(e -> {
                var j = e.getValue();
                if (j instanceof JsonObject jo) j = sortJson(jo);
                joToMap.put(e.getKey(), j);
            });
            JsonObject sortedJSON = new JsonObject();
            joToMap.forEach(sortedJSON::add);
            return sortedJSON;
        } catch (Exception ignored) {
        }
        return jsonObject;
    }
}
