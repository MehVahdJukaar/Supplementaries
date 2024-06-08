package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class PopperBehavior extends DispenserHelper.AdditionalDispenserBehavior {


    protected PopperBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource blockSource, ItemStack stack) {
        BlockPos pos = blockSource.getPos();
        Level level = blockSource.getLevel();
        var facing = blockSource.getBlockState().getValue(DispenserBlock.FACING).step();

        Vec3 p = new Vec3(pos.getX() + 0.5 - facing.x,
                pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);

        ClientBoundParticlePacket packet = new ClientBoundParticlePacket(p, ClientBoundParticlePacket.Type.CONFETTI,
                null, new Vec3(facing.mul(-1)));
        ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, pos, packet);

        stack.shrink(1);

        return InteractionResultHolder.success(stack);
    }
}
