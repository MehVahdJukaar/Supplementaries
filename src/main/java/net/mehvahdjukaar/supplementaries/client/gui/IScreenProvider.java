package net.mehvahdjukaar.supplementaries.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IScreenProvider {

    default void openScreen(Level level, BlockPos pos, Player player) {
        Screen s = this.getScreen();
        if (s != null) {
            Minecraft.getInstance().setScreen(this.getScreen());
        }
    }

    Screen getScreen();
}
