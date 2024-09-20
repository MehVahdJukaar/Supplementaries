package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.components.SelectableContainerContent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SelectableContainerContent.Mut.class)
public abstract class SelfSelectableContainerMixin implements IItemHandlerModifiable {

}
