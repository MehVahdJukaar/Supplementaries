package net.mehvahdjukaar.supplementaries.common.utils.fake_level;

import net.mehvahdjukaar.moonlight.api.misc.FakeLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTestLevel extends FakeLevel {

    public BlockState blockState;

    public static BlockTestLevel get(RegistryAccess ra) {
        // always server sie even on client as projectiles entities wont get fire on client
        return FakeLevel.get("faucet_test_level", false,ra, BlockTestLevel::new);
    }

    public BlockTestLevel(boolean clientSide, String id, RegistryAccess registryAccess) {
        super(clientSide, id, registryAccess);
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


