package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
        value = Dist.CLIENT,
        _interface = IScreenProvider.class
)
public interface ITextHolderProvider extends IScreenProvider {
    TextHolder getTextHolder();


}
