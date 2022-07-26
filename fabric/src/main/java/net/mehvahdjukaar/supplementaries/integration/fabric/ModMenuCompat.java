package net.mehvahdjukaar.supplementaries.integration.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.api.integration.ClothConfigCompat;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.SelfIExtraModelDataProvider;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if( ClientConfigs.CLIENT_SPEC instanceof FabricConfigSpec spec){
            return parent-> ClothConfigCompat.makeScreen(parent, spec, Supplementaries.res(
                    "textures/blocks/blackstone_tiles.png"
            ));
        }

        return parent -> null;
    }
}