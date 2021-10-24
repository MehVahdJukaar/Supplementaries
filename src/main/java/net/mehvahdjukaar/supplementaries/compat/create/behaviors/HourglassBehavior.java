//package net.mehvahdjukaar.supplementaries.compat.create.behaviors;
//
//
//import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
//import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
//import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
//import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
//import net.mehvahdjukaar.supplementaries.block.blocks.HourGlassBlock;
//import net.mehvahdjukaar.supplementaries.block.tiles.HourGlassBlockTile;
//import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HourGlassBlockTileRenderer;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.renderer.IRenderTypeBuffer;
//import net.minecraft.client.renderer.WorldRenderer;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.inventory.ItemStackHelper;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.util.Direction;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.vector.Vector3d;
//import net.minecraft.util.math.vector.Vector3i;
//import net.minecraft.world.gen.feature.template.Template;
//
//import java.util.Map;
//import java.util.function.UnaryOperator;
//
//public class HourglassBehavior extends MovementBehaviour {
//
//    public static void changeState(MovementContext context, BlockState newState) {
//        context.state = newState;
//        Map<BlockPos, Template.BlockInfo> blocks = context.contraption.getBlocks();
//        if (blocks.containsKey(context.localPos)) {
//            Template.BlockInfo info = blocks.get(context.localPos);
//            Template.BlockInfo newInfo = new Template.BlockInfo(info.pos,
//                    context.state, info.nbt);
//            blocks.remove(context.localPos);
//            blocks.put(context.localPos, newInfo);
//        }
//    }
//
//    @Override
//    public void tick(MovementContext context) {
//        UnaryOperator<Vector3d> rot = context.rotation;
//        BlockState state = context.state;
//        Direction dir = state.getValue(HourGlassBlock.FACING);
//        Vector3i in = dir.getNormal();
//        Vector3d v = new Vector3d(in.getX(), in.getY(), in.getZ());
//        Vector3d v2 = rot.apply(v);
//        double dot = v2.dot(new Vector3d(0, 1, 0));
//
//        CompoundNBT com = context.tileData;
//
//        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
//        float progress = com.getFloat("Progress");
//        float prevProgress = com.getFloat("PrevProgress");
//
//
//        if (!sandType.isEmpty()) {
//            prevProgress = progress;
//
//
//            //TODO: re do all of this
//
//            if (dot > 0 && progress != 1) {
//                progress = Math.min(progress + sandType.increment, 1f);
//            } else if (dot < 0 && progress != 0) {
//                progress = Math.max(progress - sandType.increment, 0f);
//            }
//
//        }
//
//        com.remove("Progress");
//        com.remove("PrevProgress");
//        com.putFloat("Progress", progress);
//        com.putFloat("PrevProgress", prevProgress);
//    }
//
//    @Override
//    public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld, ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
//
//        CompoundNBT com = context.tileData;
//        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
//        float progress = com.getFloat("Progress");
//        float prevProgress = com.getFloat("PrevProgress");
//        NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
//        ItemStackHelper.loadAllItems(com, stacks);
//        float partialTicks = 1;
//        if (sandType.isEmpty()) return;
//
//        Vector3d v = context.position;
//        if (v == null) {
//            v = new Vector3d(0, 0, 0);
//        }
//        BlockPos pos = new BlockPos(v);
//
//        int light = WorldRenderer.getLightColor(context.world, pos);
//
//        TextureAtlasSprite sprite = sandType.getSprite(stacks.get(0), renderWorld);
//
//        float h = MathHelper.lerp(partialTicks, prevProgress, progress);
//        Direction dir = context.state.getValue(HourGlassBlock.FACING);
//        HourGlassBlockTileRenderer.renderSand(matrices.getModelViewProjection(), buffer, light, 0, sprite, h, dir);
//    }
//
//}
