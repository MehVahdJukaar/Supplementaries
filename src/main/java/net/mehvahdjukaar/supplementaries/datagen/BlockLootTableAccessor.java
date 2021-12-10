package net.mehvahdjukaar.supplementaries.datagen;


import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;

public class BlockLootTableAccessor extends BlockLoot {

    public static LootTable.Builder dropping(ItemLike item) {
        return BlockLoot.createSingleItemTable(item);
    }

}