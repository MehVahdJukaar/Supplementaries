package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidsUtilImpl {

    private static final long BOTTLE = FluidConstants.BOTTLE;

    private static Storage<FluidVariant> findStorage(Level level, BlockPos pos, Direction dir, BlockEntity blockEntity) {
        return FluidStorage.SIDED.find(level, pos, level.getBlockState(pos), blockEntity, dir);
    }

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        Level level = tileBack.getLevel();
        if (level == null || amount <= 0) return false;

        Storage<FluidVariant> storage = findStorage(level, tileBack.getBlockPos(), dir, tileBack);
        if (storage == null) return false;

        long toExtract = BOTTLE * amount;

        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage) {
                if (view.isResourceBlank()) continue;

                FluidVariant resource = view.getResource();
                if (resource.isBlank()) continue;

                long extracted = storage.extract(resource, toExtract, transaction);
                if (extracted == toExtract) {
                    transaction.commit();
                    tileBack.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return findStorage(level, pos, dir, level.getBlockEntity(pos)) != null;
    }

    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer) {
        Level level = tileBelow.getLevel();
        if (level == null || offer == null || offer.fluid().isEmpty()) return null;

        Storage<FluidVariant> storage = findStorage(level, tileBelow.getBlockPos(), Direction.UP, tileBelow);
        if (storage == null) return null;

        FluidVariant variant = SoftFluidStackImpl.toFabricFluid(offer.fluid());
        if (variant.isBlank()) return null;

        long toInsert = BOTTLE * offer.minAmount();

        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(variant, toInsert, transaction);
            if (inserted <= 0) return null;

            transaction.commit();
            tileBelow.setChanged();
            return Mth.ceil(inserted / (float) BOTTLE);
        }
    }

    public static FluidOffer getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        Storage<FluidVariant> storage = findStorage(level, pos, dir, source);
        if (storage == null) return null;

        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<FluidVariant> view : storage) {
                if (view.isResourceBlank()) continue;

                FluidVariant resource = view.getResource();
                if (resource.isBlank()) continue;

                long availableBottles = Math.min(4L, view.getAmount() / BOTTLE);
                if (availableBottles <= 0) continue;

                long extracted = storage.extract(resource, availableBottles * BOTTLE, transaction);
                int actualAmount = (int) (extracted / BOTTLE);
                if (actualAmount <= 0) continue;

                SoftFluidStack softFluid = SoftFluidStackImpl.fromFabricFluid(resource, actualAmount);
                if (!softFluid.isEmpty()) {
                    return FluidOffer.of(softFluid, actualAmount);
                }
            }
        }

        return null;
    }
}
