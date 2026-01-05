package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//just here to hold the slot so we can avoid packets when it doesn't change
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements IQuiverPlayer {

    @Unique
    private SlotReference supp$lastQuiverSlot = SlotReference.EMPTY;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public SlotReference supplementaries$getQuiverSlot() {
        return supp$lastQuiverSlot;
    }

    @Override
    public void supplementaries$setQuiverSlot(SlotReference slot) {
        this.supp$lastQuiverSlot = slot;
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supp$lastQuiverSlot.get(this);
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
    }
}
