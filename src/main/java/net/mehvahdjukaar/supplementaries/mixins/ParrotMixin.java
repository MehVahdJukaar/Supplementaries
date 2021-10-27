package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.capabilities.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.capabilities.mobholder.MobContainer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ParrotMixin {

    @Inject(method = "notifyNearbyEntities", at = @At("HEAD"))
    private void setPartying(Level worldIn, BlockPos pos, boolean isPartying, CallbackInfo info) {

        int r = 3;
        BlockPos.MutableBlockPos mut = pos.mutable();
        for (int x = pos.getX() - r; x < pos.getX() + r; x++) {
            for (int y = pos.getY() - r; y < pos.getY() + r; y++) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; z++) {
                    BlockEntity te = worldIn.getBlockEntity(mut.set(x, y, z));
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
