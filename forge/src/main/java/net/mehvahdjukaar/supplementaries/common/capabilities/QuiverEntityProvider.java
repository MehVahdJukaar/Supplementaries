package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//just used for quark as caps are nice for mod communication
public class QuiverEntityProvider implements ICapabilityProvider {
    private final LazyOptional<IQuiverEntity> lazyOptional;


    public QuiverEntityProvider(Player player) {
        this.lazyOptional = LazyOptional.of(() ->
                //client already has it, server checks each time
                player instanceof IQuiverEntity qe ? qe :
                        new IQuiverEntity() {
                            @Override
                            public ItemStack supplementaries$getQuiver() {
                                return QuiverItem.getQuiver(player);
                            }

                            @Override
                            public void supplementaries$setQuiver(ItemStack quiver) {
                            }
                        });
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        return CapabilityHandler.QUIVER_PLAYER.orEmpty(capability, lazyOptional);
    }
}
