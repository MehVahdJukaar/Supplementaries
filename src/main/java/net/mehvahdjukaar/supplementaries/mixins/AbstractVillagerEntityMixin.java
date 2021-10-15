package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerEntityMixin extends AgableMob implements ICustomDataHolder {


    public boolean breeding = false;

    protected AbstractVillagerEntityMixin(EntityType<? extends AgableMob> p_i48581_1_, Level p_i48581_2_) {
        super(p_i48581_1_, p_i48581_2_);
    }

    @Override
    public boolean getVariable() {
        return breeding;
    }

    @Override
    public void setVariable(boolean val) {
        breeding = val;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte b) {
        if (b == 15) {
            this.breeding = true;
        } else if (b == 16) {
            this.breeding = false;
        } else {
            super.handleEntityEvent(b);
        }

    }
}