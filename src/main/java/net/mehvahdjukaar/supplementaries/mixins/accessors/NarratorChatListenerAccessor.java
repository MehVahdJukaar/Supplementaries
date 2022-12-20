package net.mehvahdjukaar.supplementaries.mixins.accessors;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NarratorChatListener.class)
public interface NarratorChatListenerAccessor {

    @Accessor
    Narrator getNarrator();

}