package net.mehvahdjukaar.supplementaries.common.entities.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

//Like TamableAnimal but in component form and not just for players
public class LivingEntityTamable {
    public static final Codec<LivingEntityTamable> CODEC =  UUIDUtil.CODEC.optionalFieldOf("owner")
            .xmap(u -> new LivingEntityTamable(u.orElse(null)), lo -> Optional.ofNullable(lo.owner)).codec();

    @Nullable
    private UUID owner = null;

    public LivingEntityTamable(@Nullable UUID owner) {
        this.owner = owner;
    }

    public LivingEntityTamable() {
    }

    public void setOwner(LivingEntity entity) {
        this.owner = entity.getUUID();
    }

    public boolean unableToMoveToOwner(Mob myEntity) {
        ServerLevel level = (ServerLevel) myEntity.level();
        LivingEntity owner = this.getOwner(myEntity);
        return owner != null && myEntity.distanceToSqr(owner) >= 144.0;
    }

    @Nullable
    public LivingEntity getOwner(Mob myEntity) {
        var e = ((ServerLevel) myEntity.level()).getEntity(this.owner);
        if (e instanceof LivingEntity le && le.isAlive() && (!(myEntity instanceof TamableAnimal t) || t.getOwner() == null) ) {
            return le;
        }
        this.owner = null;
        return null;
    }


}
