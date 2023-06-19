package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;


public interface ITextHolderProvider extends IScreenProvider, IWashable {

    TextHolder getTextHolder(int ind);

    default TextHolder getTextHolder() {
        return getTextHolder(0);
    }


    @Override
    default boolean tryWash(Level level, BlockPos pos, BlockState state) {
        var text = getTextHolder();
        if (!text.isEmpty(null)) {
            text.clear();
            this.setChanged();
            return true;
        }
        return false;
    }

    void setChanged();

    default boolean tryAcceptingClientText(int index, ServerPlayer player, List<FilteredText> filteredText) {
        if (!this.isWaxed() && player.getUUID().equals(this.getPlayerWhoMayEdit())) {
            var holder = this.getTextHolder(index);
            holder.acceptClientMessages(player, filteredText);
            this.setAllowedPlayerEditor(null);
            return true;
        } else {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable sign",
                    player.getName().getString());
        }
        return false;
    }

    void setAllowedPlayerEditor(UUID uuid);

    UUID getPlayerWhoMayEdit();

    boolean isWaxed();
}
