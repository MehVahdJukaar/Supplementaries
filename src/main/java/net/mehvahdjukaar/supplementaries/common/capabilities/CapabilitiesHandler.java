package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public class CapabilitiesHandler {

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ICatchableMob.class);
    }

}