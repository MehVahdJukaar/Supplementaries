package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HourGlassBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.integration.forge.CreateCompatImpl;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.UnaryOperator;

public class HourglassBehavior implements MovementBehaviour {

    private HourglassTimeData sandData;
    private float progress;
    private float prevProgress;

    private TextureAtlasSprite cachedTexture;

    @Override
    public void tick(MovementContext context) {
        UnaryOperator<Vec3> rot = context.rotation;
        BlockState state = context.state;
        Direction dir = state.getValue(HourGlassBlock.FACING);
        Rotation rotation = CreateCompatImpl.isClockWise(rot, dir);

        CompoundTag com = context.tileData;
        this.sandData = HourglassTimeData.EMPTY;
        var prevSandData = this.sandData;
        NonNullList<ItemStack> l = NonNullList.create();
        ContainerHelper.loadAllItems(com, l);
        if (l.isEmpty()) return;
        this.sandData = HourglassTimesManager.getData(l.get(0).getItem());
        if (prevSandData != sandData && context.world.isClientSide) {
            this.cachedTexture = sandData.computeTexture(l.get(0), context.world);
        }
        this.progress = com.getFloat("Progress");
        this.prevProgress = com.getFloat("PrevProgress");


        if (!sandData.isEmpty()) {
            prevProgress = progress;

            if (rotation == Rotation.CLOCKWISE_90 && progress != 1) {
                progress = Math.min(progress + sandData.getIncrement(), 1f);
            } else if (rotation == Rotation.COUNTERCLOCKWISE_90 && progress != 0) {
                progress = Math.max(progress - sandData.getIncrement(), 0f);
            }

        }

        com.remove("Progress");
        com.remove("PrevProgress");
        com.putFloat("Progress", progress);
        com.putFloat("PrevProgress", prevProgress);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        float partialTicks = 1;
        if (sandData.isEmpty()) return;

        Vec3 v = context.position;
        if (v == null) {
            v = new Vec3(0, 0, 0);
        }
        BlockPos pos = BlockPos.containing(v);

        int light = LevelRenderer.getLightColor(context.world, pos);

        float h = Mth.lerp(partialTicks, prevProgress, progress);
        Direction dir = context.state.getValue(HourGlassBlock.FACING);
        HourGlassBlockTileRenderer.renderSand(matrices.getModelViewProjection(), buffer, light, 0, cachedTexture, h, dir);
    }

}
