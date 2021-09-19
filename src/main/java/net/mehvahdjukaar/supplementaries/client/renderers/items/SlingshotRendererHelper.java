package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SlingshotRendererHelper {

    private static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;

    public static ItemStack getAmmoForPreview(ItemStack cannon, @Nullable World world, PlayerEntity player) {
        if (world != null) {
            if (world.getGameTime() % 10 == 0) {
                CLIENT_CURRENT_AMMO = ItemStack.EMPTY;

                ItemStack findAmmo = player.getProjectile(cannon);
                if (findAmmo.getItem() != Items.ARROW) {
                    CLIENT_CURRENT_AMMO = findAmmo;
                }
            }
        }
        return CLIENT_CURRENT_AMMO;
    }


    private static BlockPos LOOK_POS = null;


    public static void grabNewLookPos(PlayerEntity player){

        float blockRange = 40;

        World world = player.level;

        Vector3d start = player.position().add(0, player.getEyeHeight(), 0);
        Vector3d range = player.getLookAngle().scale(blockRange);
        BlockRayTraceResult raytrace = world
                .clip(new RayTraceContext(start, start.add(range), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
        if (raytrace != null && raytrace.getType() == RayTraceResult.Type.BLOCK &&
                start.distanceToSqr(raytrace.getLocation()) > MathHelper.square(Minecraft.getInstance().gameMode.getPickRange())) {
            LOOK_POS = raytrace.getBlockPos().relative(raytrace.getDirection(),(int) ((double)ClientConfigs.general.TEST1.get()));
        }
    }

    public static void renderBlockOutline(MatrixStack matrixStack, ActiveRenderInfo camera, Minecraft mc){
        if(LOOK_POS != null){

            PlayerEntity player = mc.player;
            World world = player.level;
            world.getProfiler().popPush("outline");
            BlockPos pos = LOOK_POS;
            BlockState blockstate = world.getBlockState(pos);
            if (!blockstate.isAir(world, pos) && world.getWorldBorder().isWithinBounds(pos)) {
                IVertexBuilder builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

                Vector3d vector3d = camera.getPosition();
                double pX = vector3d.x();
                double pY = vector3d.y();
                double pZ = vector3d.z();

                int color = ClientConfigs.cached.SLINGSHOT_OUTLINE_COLOR;

                float a = NativeImage.getR(color) / 255f;
                float b = NativeImage.getG(color) / 255f;
                float g = NativeImage.getB(color) / 255f;
                float r = NativeImage.getA(color) / 255f;

                renderVoxelShape(matrixStack, builder, blockstate.getShape(world, pos, ISelectionContext.of(camera.getEntity())),
                        (double)pos.getX() - pX, (double)pos.getY() - pY, (double)pos.getZ() - pZ, r, g, b, a);
            }
        }
        LOOK_POS = null;
    }

    private static void renderVoxelShape(MatrixStack matrixStack, IVertexBuilder builder, VoxelShape
            voxelShape, double x, double y, double z, float r, float g, float b, float a) {
        Matrix4f matrix4f = matrixStack.last().pose();
        voxelShape.forAllEdges((e1, e2, e3, e4, e5, e6) -> {
            builder.vertex(matrix4f, (float)(e1 + x), (float)(e2 + y), (float)(e3 + z)).color(r, g, b, a).endVertex();
            builder.vertex(matrix4f, (float)(e4 + x), (float)(e5 + y), (float)(e6 + z)).color(r, g, b, a).endVertex();
        });
    }


}
