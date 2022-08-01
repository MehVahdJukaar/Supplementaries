package net.mehvahdjukaar.supplementaries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SupplementariesClient {

    private static float partialTicks = 0;

    public static void initClient() {

    }

    public static float getPartialTicks(){
        return partialTicks;
    }

    public static void onRenderTick(float ticks){
        partialTicks = ticks;
    }
    //TODO: move client setup here



}
