package net.mehvahdjukaar.supplementaries.setup;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;


public class Dispenser {

    public static void registerBehaviors() {
        for(Item item : ForgeRegistries.ITEMS) {
            if(item == new ItemStack(Registry.JAR.get()).getItem()){
                register(item, new JarDispenseBehavior());
            }
        }
    }

    private static void register(IItemProvider provider, IDispenseItemBehavior behavior) {
        DispenserBlock.registerDispenseBehavior(provider, behavior);
    }

    public static class JarDispenseBehavior extends OptionalDispenseBehavior {
        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            this.setSuccessful(false);
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Direction direction = source.getBlockState().get(DispenserBlock.FACING);
                BlockPos blockpos = source.getBlockPos().offset(direction);
                Direction direction1 = source.getWorld().isAirBlock(blockpos.down()) ? direction : Direction.UP;
                this.setSuccessful(((BlockItem)item).tryPlace(new DirectionalPlaceContext(source.getWorld(), blockpos, direction, stack, direction1)) == ActionResultType.SUCCESS);
            }

            return stack;
        }
    }
}