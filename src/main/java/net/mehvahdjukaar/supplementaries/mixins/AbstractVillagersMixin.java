package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.util.ICustomDataHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagersMixin extends AgeableMob implements ICustomDataHolder {

    public boolean fricking = false;

    protected AbstractVillagersMixin(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean getVariable() {
        return fricking;
    }

    @Override
    public void setVariable(boolean val) {
        fricking = val;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte b) {
        if (b == 15) {
            this.fricking = true;
        } else if (b == 16) {
            this.fricking = false;
        } else {
            super.handleEntityEvent(b);
        }

    }
}