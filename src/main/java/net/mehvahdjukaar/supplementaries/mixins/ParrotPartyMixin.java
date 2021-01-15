package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.IMobHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.inventory.container.ShulkerBoxSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class ParrotPartyMixin {

    @Inject(method = "setPartying", at = @At("HEAD"), cancellable = true)
    private void setPartying(World worldIn, BlockPos pos, boolean isPartying, CallbackInfo info) {
        int r = 3;
        BlockPos.Mutable mut = pos.toMutable();
        for (int x = pos.getX() - r; x < pos.getX() + r; x++) {
            for (int y = pos.getY() - r; y < pos.getY() + r; y++) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; z++) {
                    TileEntity te = worldIn.getTileEntity(mut.setPos(x, y, z));
                    if (te instanceof IMobHolder) {
                        ((IMobHolder) te).getMobHolder().setPartying(pos, isPartying);
                    }
                }
            }
        }
    }
}
