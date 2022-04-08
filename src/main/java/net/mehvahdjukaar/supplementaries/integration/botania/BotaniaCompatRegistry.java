package net.mehvahdjukaar.supplementaries.integration.botania;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;
import vazkii.botania.common.block.tile.TileTinyPotato;

public class BotaniaCompatRegistry {
    public static final String TATER_IN_A_JAR_NAME = "tater_in_a_jar";

    public static final RegistryObject<Block> TATER_IN_A_JAR;
    public static final RegistryObject<BlockEntityType<TaterInAJarBlockTile>> TATER_IN_A_JAR_TILE;
    public static final RegistryObject<Item> TATER_IN_A_JAR_ITEM;

    static {
        TATER_IN_A_JAR = ModRegistry.BLOCKS.register(TATER_IN_A_JAR_NAME, () -> new TaterInAJarBlock(
                BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                        .strength(1f, 1f)
                        .sound(SoundType.GLASS)
                        .noOcclusion()));
        TATER_IN_A_JAR_ITEM = RegistryHelper.regItem(TATER_IN_A_JAR_NAME, () -> new BlockItem(TATER_IN_A_JAR.get(),
                (new Item.Properties()).tab(null).rarity(Rarity.UNCOMMON)));
        TATER_IN_A_JAR_TILE = ModRegistry.TILES.register(TATER_IN_A_JAR_NAME, () -> BlockEntityType.Builder.of(
                TaterInAJarBlockTile::new, TATER_IN_A_JAR.get()).build(null));
    }

    public static void registerStuff() {
    }

    public static InteractionResult tryCaptureTater(AbstractMobContainerItem item, UseOnContext context) {

        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        if (world.getBlockEntity(pos) instanceof TileTinyPotato te && te.getType() != TATER_IN_A_JAR_TILE.get()) {
            ItemStack stack = context.getItemInHand();
            CompoundTag com = stack.getTagElement("BlockEntityTag");
            if (com == null || com.isEmpty()) {
                if (!world.isClientSide) {
                    Player player = context.getPlayer();
                    item.playCatchSound(player);

                    ItemStack returnItem = new ItemStack(TATER_IN_A_JAR_ITEM.get());
                    if (te.hasCustomName())
                        returnItem.setHoverName(te.getCustomName());
                    Utils.swapItemNBT(player, context.getHand(), stack, returnItem);

                    world.removeBlock(pos, false);

                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
