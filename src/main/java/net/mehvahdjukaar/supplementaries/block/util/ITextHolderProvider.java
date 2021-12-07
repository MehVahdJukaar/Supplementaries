package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ITextHolderProvider {
    TextHolder getTextHolder();

    default void openTextEditScreen(World level, BlockPos pos, PlayerEntity player) {
        Screen s = this.getTextEditScreen();
        if (s != null) {
            Minecraft.getInstance().setScreen(this.getTextEditScreen());
        }
    }

    @Nullable
    Screen getTextEditScreen();
}
