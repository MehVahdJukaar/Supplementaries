package net.mehvahdjukaar.supplementaries.integration.create.behaviors;


import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HourGlassBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HourGlassBlockTile;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.UnaryOperator;

public class HourglassBehavior extends MovementBehaviour {

    @Override
    public void tick(MovementContext context) {
        UnaryOperator<Vec3> rot = context.rotation;
        BlockState state = context.state;
        Direction dir = state.getValue(HourGlassBlock.FACING);
        var in = dir.getNormal();
        Vec3 v = new Vec3(in.getX(), in.getY(), in.getZ());
        Vec3 v2 = rot.apply(v);
        double dot = v2.dot(new Vec3(0, 1, 0));

        CompoundTag com = context.tileData;

        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
        float progress = com.getFloat("Progress");
        float prevProgress = com.getFloat("PrevProgress");


        if (!sandType.isEmpty()) {
            prevProgress = progress;


            //TODO: re do all of this

            if (dot > 0 && progress != 1) {
                progress = Math.min(progress + sandType.increment, 1f);
            } else if (dot < 0 && progress != 0) {
                progress = Math.max(progress - sandType.increment, 0f);
            }

        }

        com.remove("Progress");
        com.remove("PrevProgress");
        com.putFloat("Progress", progress);
        com.putFloat("PrevProgress", prevProgress);
    }

    @Override
    public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {

        CompoundTag com = context.tileData;
        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
        float progress = com.getFloat("Progress");
        float prevProgress = com.getFloat("PrevProgress");
        NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(com, stacks);
        float partialTicks = 1;
        if (sandType.isEmpty()) return;

        Vec3 v = context.position;
        if (v == null) {
            v = new Vec3(0, 0, 0);
        }
        BlockPos pos = new BlockPos(v);

        int light = LevelRenderer.getLightColor(context.world, pos);

        TextureAtlasSprite sprite = sandType.getSprite(stacks.get(0), renderWorld);

        float h = Mth.lerp(partialTicks, prevProgress, progress);
        Direction dir = context.state.getValue(HourGlassBlock.FACING);
        HourGlassBlockTileRenderer.renderSand(matrices.getModelViewProjection(), buffer, light, 0, sprite, h, dir);
    }

}
