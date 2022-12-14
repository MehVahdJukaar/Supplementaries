package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.misc.mob_container.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class ParrotMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(method = "notifyNearbyEntities", at = @At("HEAD"))
    private void setPartying(Level worldIn, BlockPos pos, boolean isPartying, CallbackInfo info) {
        List<Player> list = level.getEntitiesOfClass(Player.class, (new AABB(pos)).inflate(3.0));

        for (Player player : list) {
            var l = player.getShoulderEntityLeft();
            if (l != null) l.putBoolean("record_playing", isPartying);
            var r = player.getShoulderEntityRight();
            if (r != null) r.putBoolean("record_playing", isPartying);

        }
        int r = 3;
        BlockPos.MutableBlockPos mut = pos.mutable();
        for (int x = pos.getX() - r; x < pos.getX() + r; x++) {
            for (int y = pos.getY() - r; y < pos.getY() + r; y++) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; z++) {
                    if (worldIn.getBlockEntity(mut.set(x, y, z)) instanceof IMobContainerProvider te) {
                        MobContainer container = te.getMobContainer();
                        Entity e = container.getDisplayedMob();
                        if (e instanceof LivingEntity le) le.setRecordPlayingNearby(pos, isPartying);
                    }
                }
            }
        }
    }
}
