package net.mehvahdjukaar.supplementaries.integration.flywheel;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.part.InstanceTree;
import dev.engine_room.flywheel.lib.model.part.ModelTrees;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BellowsBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BundleItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class BellowsInstance extends AbstractBlockEntityVisual<BellowsBlockTile> implements SimpleDynamicVisual {
    private static final Material MATERIAL = SimpleMaterial.builder()
            .cutout(CutoutShaders.ONE_TENTH)
            .texture(TextureAtlas.LOCATION_BLOCKS)
            .mipmap(false)
            .backfaceCulling(false)
            .build();

    private final InstanceTree instances;
    private final InstanceTree center;
    private final InstanceTree top;
    private final InstanceTree bottom;
    private final InstanceTree leather;

    private final Matrix4f initialPose;

    private float lastProgress = Float.NaN;

    public BellowsInstance(VisualizationContext ctx, BellowsBlockTile blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        this.instances = InstanceTree.create(instancerProvider(),
                ModelTrees.of(ClientRegistry.BELLOWS_MODEL, ModMaterials.BELLOWS_MATERIAL, MATERIAL));

        this.leather = instances.child("leather");
        this.top = instances.child("top");
        this.bottom = instances.child("bottom");
        this.center = instances.child("center");


        initialPose = createInitialPose();
    }

    private Matrix4f createInitialPose() {
        var visualPosition = getVisualPosition();
        var rotation = getDirection().getRotation();
        return new Matrix4f().translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
                .translate(0.5f, 0.5f, 0.5f)
                .scale(0.9995f)
                .rotate(rotation)
                .scale(1, -1, -1)
                .translate(0, -1, 0);
    }


    @Override
    public void beginFrame(Context context) {

        float dh = blockEntity.getHeight(context.partialTick());

        /*
        this.stack.pushPose();

        this.stack.translate(0, -1 + (3 / 16d) - dh, 0);
        this.top.setTransform(this.stack);

        this.stack.popPose();


        this.stack.pushPose();

        this.stack.translate(0, dh, 0);
        this.bottom.setTransform(this.stack);

        this.stack.popPose();


        this.stack.pushPose();

        float j = 3.2f;
        this.stack.scale(1, 1 + j * dh, 1);
        this.leather.setTransform(this.stack);

        this.stack.popPose();

         */
    }

    @Override
    protected void _delete() {
        this.instances.delete();
    }

    @Override
    public void updateLight(float v) {
        int packedLight = computePackedLight();
        instances.traverse(instance -> {
            instance.light(packedLight)
                    .setChanged();
        });
    }

    private Direction getDirection() {
        return this.blockState.getValue(BellowsBlock.FACING);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        instances.traverse(consumer);
    }


}
