package net.mehvahdjukaar.supplementaries.plugins.create.behaviors;



/*

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

public class HourglassBehavior extends MovementBehaviour {

    public static void changeState(MovementContext context, BlockState newState){
        context.state = newState;
        Map<BlockPos, Template.BlockInfo> blocks = context.contraption.getBlocks();
        if(blocks.containsKey(context.localPos)){
            Template.BlockInfo info = blocks.get(context.localPos);
            Template.BlockInfo newInfo = new Template.BlockInfo(info.pos,
                    context.state, info.nbt);
            blocks.remove(context.localPos);
            blocks.put(context.localPos,newInfo);
        }
    }

    @Override
    public void tick(MovementContext context) {
        UnaryOperator<Vector3d> rot = context.rotation;
        BlockState state = context.state;
        Direction dir = state.getValue(HourGlassBlock.FACING);
        Vector3i in = dir.getNormal();
        Vector3d v = new Vector3d(in.getX(), in.getY(), in.getZ());
        Vector3d v2 = rot.apply(v);
        double dot = v2.dot(new Vector3d(0,1,0));

        CompoundNBT com = context.tileData;

        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
        float progress = com.getFloat("Progress");
        float prevProgress = com.getFloat("PrevProgress");


        if(!sandType.isEmpty()) {
            prevProgress = progress;


            //TODO: re do all of this

            if (dot>0 && progress != 1) {
                progress = Math.min(progress + sandType.increment, 1f);
            } else if (dot<0  && progress != 0) {
                progress = Math.max(progress - sandType.increment, 0f);
            }

        }

        com.remove("Progress");
        com.remove("PrevProgress");
        com.putFloat("Progress",progress);
        com.putFloat("PrevProgress",prevProgress);

    }

    @Override
    public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal, IRenderTypeBuffer buffer) {
        CompoundNBT com = context.tileData;
        HourGlassBlockTile.HourGlassSandType sandType = HourGlassBlockTile.HourGlassSandType.values()[com.getInt("SandType")];
        float progress = com.getFloat("Progress");
        float prevProgress = com.getFloat("PrevProgress");
        float partialTicks = 1;
        if(sandType.isEmpty())return;

        Vector3d v = context.position;
        if(v==null){
            v = new Vector3d(0,0,0);
        }
        BlockPos pos = new BlockPos(v);

        int light = WorldRenderer.getLightColor(context.world, pos);

        TextureAtlasSprite sprite = sandType.getSprite(Items.REDSTONE);

        float h = MathHelper.lerp(partialTicks, prevProgress, progress);
        Direction dir = context.state.getValue(HourGlassBlock.FACING);
        renderSand(ms,buffer,light,0,sprite,h,dir);
    }

}*/
