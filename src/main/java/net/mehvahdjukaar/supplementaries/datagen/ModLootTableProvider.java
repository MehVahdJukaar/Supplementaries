package net.mehvahdjukaar.supplementaries.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModLootTableProvider extends LootTableProvider {
    private final DataGenerator generator;
    protected final Map<ResourceLocation, LootTable> tables = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public ModLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
    }

    @Override
    public void run(HashCache cache) {

        for(DyeColor color : DyeColor.values()){
            //addBlockLoot(Registry.FLAGS.get(color).get());
        }
        for(DyeColor color : DyeColor.values()){
            addBlockLoot(ModRegistry.PRESENTS.get(color).get());
        }


        for(IWoodType wood : WoodTypes.TYPES.values()){
            //addBlockLoot(Registry.HANGING_SIGNS.get(wood).get());
        }



        saveTables(cache, tables);
    }

    public void saveTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
            } catch (IOException ignored) {}
        });
    }


    public void addBlockLoot(Block block) {
        tables.put(block.getLootTable(), BlockLootTableAccessor.dropping(block).setParamSet(LootContextParamSets.BLOCK).build());
    }
}