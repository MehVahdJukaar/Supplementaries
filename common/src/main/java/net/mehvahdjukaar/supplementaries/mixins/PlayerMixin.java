package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerMixin implements IQuiverEntity {
    @Override
    public ItemStack supplementaries$getQuiver() {
        return ItemStack.EMPTY;
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
    }


}
