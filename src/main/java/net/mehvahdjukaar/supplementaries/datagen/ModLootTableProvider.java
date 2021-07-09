package net.mehvahdjukaar.supplementaries.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.util.ResourceLocation;

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
    public void run(DirectoryCache cache) {

        for(DyeColor color : DyeColor.values()){
            //addBlockLoot(Registry.FLAGS.get(color).get());
        }


        for(IWoodType wood : WoodTypes.TYPES.values()){
            addBlockLoot(Registry.HANGING_SIGNS.get(wood).get());
        }



        saveTables(cache, tables);
    }

    public void saveTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), path);
            } catch (IOException ignored) {}
        });
    }


    public void addBlockLoot(Block block) {
        tables.put(block.getLootTable(), BlockLootTableAccessor.dropping(block).setParamSet(LootParameterSets.BLOCK).build());
    }
}