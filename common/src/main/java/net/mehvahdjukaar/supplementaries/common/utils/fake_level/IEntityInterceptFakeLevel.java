package net.mehvahdjukaar.supplementaries.common.utils.fake_level;

import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevel;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeServerLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IEntityInterceptFakeLevel {

    void setup();

    @Nullable
    Entity getIntercepted();

    static <L extends Level & IEntityInterceptFakeLevel> L get(Level level) {
        // always server sie even on client as projectiles entities wont get fire on client
        return (L) FakeLevelManager.get("projectile_test_level", level, ClientSide::new,
                ServerSide::new);
    }

    Level cast();

    class ClientSide extends FakeLevel implements IEntityInterceptFakeLevel {
        @Nullable
        public Entity projectile = null;

        public ClientSide(String name, RegistryAccess ra) {
            super(true, name, ra);
        }

        public void setup() {
            projectile = null;
        }

        @Override
        public Level cast() {
            return this;
        }

        @Nullable
        public Entity getIntercepted() {
            return projectile;
        }

        @Override
        public boolean addFreshEntity(Entity entity) {
            this.projectile = entity;
            return true;
        }
    }

    class ServerSide extends FakeServerLevel implements IEntityInterceptFakeLevel {
        @Nullable
        public Entity projectile = null;

        public ServerSide(String name, ServerLevel original) {
            super(name, original);
        }

        public void setup() {
            projectile = null;
        }

        @Override
        public Level cast() {
            return this;
        }

        @Override
        public boolean addFreshEntity(Entity entity) {
            this.projectile = entity;
            return true;
        }

        @Override
        public @Nullable Entity getIntercepted() {
            return projectile;
        }
    }
}