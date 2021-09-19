package net.mehvahdjukaar.supplementaries.compat.optifine;

import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;



public class OptifineHandler {

    private static final boolean optifineInstalled;

    static{
        optifineInstalled = Package.getPackage("net.optifine") != null;
        refresh();
    }

    /**
     * From Flywheel. Credits to JozuFozu
     */
    public static void refresh() {
        boolean shadersOff = true;
        if (optifineInstalled) {

            File dir = Minecraft.getInstance().gameDirectory;
            File shaderOptions = new File(dir, "optionsshaders.txt");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(shaderOptions));

                shadersOff = reader.lines()
                        .anyMatch(it -> {
                            String line = it.replaceAll("\\s", "");
                            if (line.startsWith("shaderPack=")) {
                                String setting = line.substring("shaderPack=".length());

                                return setting.equals("OFF") || setting.equals("(internal)");
                            }
                            return false;
                        });
            } catch (FileNotFoundException ignored) {}
        }
        RendererUtil.changeVertexFormat(shadersOff ? 8 : 9);
    }

}
