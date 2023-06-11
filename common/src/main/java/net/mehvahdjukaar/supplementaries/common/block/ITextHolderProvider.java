package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public interface ITextHolderProvider extends IScreenProvider, IWashable {

    TextHolder getTextHolder(int ind);

    default TextHolder getTextHolder(){
        return getTextHolder(0);
    }


    @Override
    default boolean tryWash(Level level, BlockPos pos, BlockState state) {
        var text = getTextHolder();
        if(!text.isEmpty(null)){
            text.clear();
            this.setChanged();
            return true;
        }
        return false;
    }

    void setChanged();

}
