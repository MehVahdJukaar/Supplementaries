package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.IrisCompat;
import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Supplier;

@Deprecated(forRemoval = true)
public class CoreShaderContainer implements Supplier<ShaderInstance> {

    private final Supplier<ShaderInstance> vanillaFallback;
    private ShaderInstance instance;

    public CoreShaderContainer(Supplier<ShaderInstance> vanillaFallback) {
        this.vanillaFallback = vanillaFallback;
    }

    public void assign(ShaderInstance instance) {
        this.instance = instance;
    }

    @Override
    public ShaderInstance get() {
        if (CompatHandler.IRIS && IrisCompat.isIrisShaderFuckerActive()) {
            return vanillaFallback.get();
        }
        return instance;
    }
}
