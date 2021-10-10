package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.mobholder.MobContainer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class ParrotPartingMixin {

    @Inject(method = "notifyNearbyEntities", at = @At("HEAD"), cancellable = true)
    private void setPartying(World worldIn, BlockPos pos, boolean isPartying, CallbackInfo info) {

        int r = 3;
        BlockPos.Mutable mut = pos.mutable();
        for (int x = pos.getX() - r; x < pos.getX() + r; x++) {
            for (int y = pos.getY() - r; y < pos.getY() + r; y++) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; z++) {
                    TileEntity te = worldIn.getBlockEntity(mut.set(x, y, z));
                    if (te instanceof IMobContainerProvider) {
                        MobContainer container = ((IMobContainerProvider) te).getMobContainer();

                        Entity e = container.getDisplayedMob();
                        if (e instanceof LivingEntity)
                            ((LivingEntity) e).setRecordPlayingNearby(pos, isPartying);

                    }
                }
            }
        }
    }
}
