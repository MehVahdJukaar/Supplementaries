package net.mehvahdjukaar.supplementaries.common.utils.fake_level;

import net.mehvahdjukaar.moonlight.api.misc.FakeLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ProjectileTestLevel extends FakeLevel {

    public static ProjectileTestLevel get(RegistryAccess ra) {
        // always server sie even on client as projectiles entities wont get fire on client
        return FakeLevel.get("cannon_test_level", false, ra, ProjectileTestLevel::new);
    }

    @Nullable
    public Entity projectile = null;

    public ProjectileTestLevel(boolean clientSide, String id, RegistryAccess ra) {
        super(clientSide, id, ra);
    }

    public void setup() {
        projectile = null;
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        this.projectile = entity;
        return true;
    }
}