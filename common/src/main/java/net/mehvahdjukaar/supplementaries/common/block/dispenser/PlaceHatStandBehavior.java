package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.function.Consumer;

public class PlaceHatStandBehavior extends DispenserHelper.AdditionalDispenserBehavior {
    public PlaceHatStandBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource blockSource, ItemStack itemStack) {
        Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos blockPos = blockSource.getPos().relative(direction);
        ServerLevel serverLevel = blockSource.getLevel();
        Consumer<HatStandEntity> consumer = EntityType.appendDefaultStackConfig((armorStandx) -> {
            armorStandx.setYRot(direction.toYRot());
        }, serverLevel, itemStack, null);
        HatStandEntity armorStand = ModEntities.HAT_STAND.get().spawn(serverLevel, itemStack.getTag(),
                consumer, blockPos, MobSpawnType.DISPENSER, false, false);
        if (armorStand != null) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.success(itemStack);
    }
}
