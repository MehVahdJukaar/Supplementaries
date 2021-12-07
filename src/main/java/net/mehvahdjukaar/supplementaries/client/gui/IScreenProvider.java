package net.mehvahdjukaar.supplementaries.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public interface IScreenProvider {

    default void openScreen(World level, BlockPos pos, PlayerEntity player) {
        Screen s = this.getScreen();
        if (s != null) {
            Minecraft.getInstance().setScreen(this.getScreen());
        }
    }

    Screen getScreen();
}
