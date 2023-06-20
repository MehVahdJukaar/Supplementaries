package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoormatBlockTile;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;


//replicates what SignBlock does + more
public interface ITextHolderProvider extends IOnePlayerGui, IWashable, IWaxable {

    TextHolder getTextHolder(int ind);

    default TextHolder getTextHolder() {
        return getTextHolder(0);
    }

    default int textHoldersCount(){
        return 1;
    }

    @Override
    default boolean tryWash(Level level, BlockPos pos, BlockState state) {
        if(this.isWaxed()){
            this.setWaxed(false);
            return true;
        }
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
            this.setPlayerWhoMayEdit(null);
            return true;
        } else {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable sign",
                    player.getName().getString());
        }
        return false;
    }


    @Override
    default boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos) {
        boolean filtering = player.isTextFilteringEnabled();
        for(int i = 0; i <this.textHoldersCount(); i++){
            if(!this.getTextHolder(i).hasEditableText(filtering)){
                return false;
            }
        }
        return IOnePlayerGui.super.tryOpeningEditGui(player, pos);
    }
}
