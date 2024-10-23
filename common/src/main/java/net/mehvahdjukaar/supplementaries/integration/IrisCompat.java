package net.mehvahdjukaar.supplementaries.integration;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;

@Deprecated(forRemoval = true)
public class IrisCompat {

    public static boolean isIrisShaderFuckerActive() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        return pipeline instanceof ShaderRenderingPipeline s && s.shouldOverrideShaders();
    }

}
