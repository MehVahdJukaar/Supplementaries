package net.mehvahdjukaar.supplementaries.common.utils.fake_level;

import net.mehvahdjukaar.moonlight.core.misc.FakeLevel;
import net.mehvahdjukaar.moonlight.core.misc.FakeLevelManager;
import net.mehvahdjukaar.moonlight.core.misc.FakeServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTestLevel extends FakeServerLevel {

    public BlockState blockState;

    public BlockTestLevel(String name, ServerLevel original) {
        super(name, original);
    }

    public static BlockTestLevel get(ServerLevel level) {
        // always server sie even on client as projectiles entities wont get fire on client
        return FakeLevelManager.getServer("faucet_test_level", level, BlockTestLevel::new);
    }


    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        this.blockState = state;
        return true;
    }

    public void setup() {
        blockState = null;
    }
}


