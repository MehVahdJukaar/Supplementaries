package net.mehvahdjukaar.supplementaries.client.renderers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SlingshotRendererHelper {

    private static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;

    public static ItemStack getAmmoForPreview(ItemStack cannon, @Nullable Level world, Player player) {
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


    public static void grabNewLookPos(Player player){

        float blockRange = 40;

        Level world = player.level;

        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 range = player.getLookAngle().scale(blockRange);
        BlockHitResult raytrace = world
                .clip(new ClipContext(start, start.add(range), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (raytrace.getType() == HitResult.Type.BLOCK && start.distanceToSqr(raytrace.getLocation()) > Mth.square(Minecraft.getInstance().gameMode.getPickRange())) {
            LOOK_POS = raytrace.getBlockPos().relative(raytrace.getDirection(),(int) ((double)ClientConfigs.general.TEST1.get()));
        }
    }

    public static void renderBlockOutline(PoseStack matrixStack, Camera camera, Minecraft mc){
        if(LOOK_POS != null){

            Player player = mc.player;
            Level world = player.level;
            world.getProfiler().popPush("outline");
            BlockPos pos = LOOK_POS;
            BlockState blockstate = world.getBlockState(pos);
            if (!blockstate.isAir(world, pos) && world.getWorldBorder().isWithinBounds(pos)) {
                VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

                Vec3 vector3d = camera.getPosition();
                double pX = vector3d.x();
                double pY = vector3d.y();
                double pZ = vector3d.z();

                int color = ClientConfigs.cached.SLINGSHOT_OUTLINE_COLOR;

                float a = NativeImage.getR(color) / 255f;
                float b = NativeImage.getG(color) / 255f;
                float g = NativeImage.getB(color) / 255f;
                float r = NativeImage.getA(color) / 255f;

                renderVoxelShape(matrixStack, builder, blockstate.getShape(world, pos, CollisionContext.of(camera.getEntity())),
                        (double)pos.getX() - pX, (double)pos.getY() - pY, (double)pos.getZ() - pZ, r, g, b, a);
            }
        }
        LOOK_POS = null;
    }

    private static void renderVoxelShape(PoseStack matrixStack, VertexConsumer builder, VoxelShape
            voxelShape, double x, double y, double z, float r, float g, float b, float a) {
        Matrix4f matrix4f = matrixStack.last().pose();
        voxelShape.forAllEdges((e1, e2, e3, e4, e5, e6) -> {
            builder.vertex(matrix4f, (float)(e1 + x), (float)(e2 + y), (float)(e3 + z)).color(r, g, b, a).endVertex();
            builder.vertex(matrix4f, (float)(e4 + x), (float)(e5 + y), (float)(e6 + z)).color(r, g, b, a).endVertex();
        });
    }


}
