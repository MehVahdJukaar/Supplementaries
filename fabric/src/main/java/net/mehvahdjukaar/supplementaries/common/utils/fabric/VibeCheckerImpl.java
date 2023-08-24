package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class VibeCheckerImpl {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void checkVibe() {
       // crashIfFabricRenderingAPIHasBeenNuked();

        fixSodiumDeps();
    }

    private static void fixSodiumDeps() {
        JsonElement obj = new JsonObject();
        var file = FabricLoader.getInstance().getConfigDir().resolve("fabric_loader_dependencies.json").toFile();
        if (file.exists() && file.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                obj = GSON.fromJson(bufferedReader, JsonElement.class);
            } catch (IOException ignored) {
            }
        }
        if (obj instanceof JsonObject jo) {
            if(!jo.has("version")){
                jo.addProperty("version", 1);
            }
            JsonObject overrides = new JsonObject();
            if(jo.has("overrides")){
                overrides = jo.getAsJsonObject("overrides");
            }
            JsonObject prop = new JsonObject();
            JsonObject dep = new JsonObject();
            dep.addProperty("indium", "*");
            prop.add("+depends", dep);
            JsonObject dep2 = new JsonObject();
            dep2.addProperty("fabric-renderer-indigo", "*");
            prop.add("+conflicts", dep2);
            overrides.add("sodium", prop);
            jo.add("overrides", overrides);

        }

        try (FileOutputStream stream = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

            GSON.toJson(obj, writer);
        } catch (IOException ignored) {
        }
    }
    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    //you were supposed to destroy the loader api nuking mods, not join them!
    public static void crashIfFabricRenderingAPIHasBeenNuked() {

        if (PlatHelper.isModLoaded("sodium") && !PlatHelper.isModLoaded("indium")) {
            Supplementaries.LOGGER.error("You seem to have installed Sodium which has been known to break fabric rendering API." +
                    "Things might not work well");
        }
    }

}
