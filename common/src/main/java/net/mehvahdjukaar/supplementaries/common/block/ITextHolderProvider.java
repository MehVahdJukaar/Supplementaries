package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public interface ITextHolderProvider extends IScreenProvider, ISoapWashable {

    TextHolder getTextHolder();

    @Override
    default boolean tryWash(Level level, BlockPos pos, BlockState state) {
        var text = getTextHolder();
        if(!text.isEmpty()){
            text.clear();
            this.setChanged();
            return true;
        }
        return false;
    }

    void setChanged();

}
