package net.mehvahdjukaar.supplementaries.compat.botania;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import vazkii.botania.common.block.tile.TileTinyPotato;

public class BotaniaCompatRegistry {
    public static final String TATER_IN_A_JAR_NAME = "tater_in_a_jar";

    public static final RegistryObject<Block> TATER_IN_A_JAR;
    public static final RegistryObject<TileEntityType<TaterInAJarBlockTile>> TATER_IN_A_JAR_TILE;
    public static final RegistryObject<Item> TATER_IN_A_JAR_ITEM;

    static {
        TATER_IN_A_JAR = ModRegistry.BLOCKS.register(TATER_IN_A_JAR_NAME, () -> new TaterInAJarBlock(
                AbstractBlock.Properties.copy(ModRegistry.JAR.get())));
        TATER_IN_A_JAR_ITEM = ModRegistry.regItem(TATER_IN_A_JAR_NAME, () -> new BlockItem(TATER_IN_A_JAR.get(),
                (new Item.Properties()).tab(null).rarity(Rarity.UNCOMMON)));
        TATER_IN_A_JAR_TILE = ModRegistry.TILES.register(TATER_IN_A_JAR_NAME, () -> TileEntityType.Builder.of(
                TaterInAJarBlockTile::new, TATER_IN_A_JAR.get()).build(null));
    }

    public static void registerStuff() {
    }

    public static ActionResultType tryCaptureTater(AbstractMobContainerItem item, ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        World world = context.getLevel();
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileTinyPotato && te.getType() != TATER_IN_A_JAR_TILE.get()) {
            ItemStack stack = context.getItemInHand();
            CompoundNBT com = stack.getTagElement("BlockEntityTag");
            if (com == null || com.isEmpty()) {
                if (!world.isClientSide) {
                    PlayerEntity player = context.getPlayer();
                    item.playCatchSound(player);

                    ItemStack returnItem = new ItemStack(TATER_IN_A_JAR_ITEM.get());
                    if (((TileTinyPotato) te).hasCustomName())
                        returnItem.setHoverName(((TileTinyPotato) te).getCustomName());
                    Utils.swapItemNBT(player, context.getHand(), stack, returnItem);

                    world.removeBlock(pos, false);

                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return ActionResultType.PASS;
    }
}
