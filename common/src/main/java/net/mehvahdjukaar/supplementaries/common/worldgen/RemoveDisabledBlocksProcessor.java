package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public class RemoveDisabledBlocksProcessor extends StructureProcessor {

    public static final RemoveDisabledBlocksProcessor INSTANCE = new RemoveDisabledBlocksProcessor();
    public static final MapCodec<RemoveDisabledBlocksProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private record Replacement(String config, UnaryOperator<BlockState> transformer) {

        public boolean isEnabled() {
            return CommonConfigs.isEnabled(config);
        }

        public BlockState transform(BlockState state) {
            return transformer.apply(state);
        }
    }

    private static final Map<Block, Replacement> REPLACEMENTS = new HashMap<>();

    public static void setup() {
        add(ModRegistry.CANNON, Blocks.AIR);
        add(ModRegistry.URN, Blocks.AIR);
        add(ModRegistry.STICK_BLOCK, Blocks.AIR);
        add(ModRegistry.GLOBE, Blocks.AIR);
        add(ModRegistry.CANNONBALL, Blocks.AIR);
        add(ModRegistry.JAR, Blocks.AIR);
        add(ModRegistry.ASH_BLOCK, Blocks.AIR);
        addAll(ModRegistry.FLAGS.values(), ModConstants.FLAG_NAME, Blocks.AIR);
        add(ModRegistry.SAFE, Blocks.CHEST);
        add(ModRegistry.ROPE_KNOT, Blocks.IRON_BARS);
        add(ModRegistry.ROPE, Blocks.IRON_BARS);
        addAll(ModRegistry.CANDLE_HOLDERS.values(), ModConstants.CANDLE_HOLDER_NAME, Blocks.TORCH);
        add(ModRegistry.GOLD_DOOR, Blocks.DARK_OAK_DOOR);
        add(ModRegistry.GOLD_TRAPDOOR, Blocks.DARK_OAK_DOOR);
        add(ModRegistry.GOLD_BARS, Blocks.IRON_BARS);
        add(ModRegistry.SCONCE, ModConstants.SCONCE_NAME, Blocks.TORCH);
        add(ModRegistry.SCONCE_GREEN, ModConstants.SCONCE_NAME, Blocks.WALL_TORCH);
        add(ModRegistry.SCONCE_SOUL, ModConstants.SCONCE_NAME, Blocks.SOUL_TORCH);
        add(ModRegistry.SCONCE_WALL_SOUL, ModConstants.SCONCE_NAME, Blocks.SOUL_WALL_TORCH);
    }

    private static void add(Block from, Replacement replacement) {
        REPLACEMENTS.put(from, replacement);
    }

    private static void add(Supplier<? extends Block> from, String configKey, Block to) {
        add(from.get(), new Replacement(configKey, to::withPropertiesOf));
    }

    private static <A extends Block> void addAll(Iterable<Supplier<A>> fromBlocks, String configKey, Block to) {
        for (Supplier<? extends Block> from : fromBlocks) {
            add(from, configKey, to);
        }
    }

    private static void add(Supplier<? extends Block> from, Block to) {
        add(from, Utils.getID(from.get()).getPath(), to);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModWorldgen.REMOVE_DISABLED_PROCESSOR.get();
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        BlockState blockState = relativeBlockInfo.state();
        Replacement replacement = REPLACEMENTS.get(blockState.getBlock());
        if (replacement == null || replacement.isEnabled()) {
            return relativeBlockInfo;
        } else {
            BlockState blockState2 = replacement.transform(blockState);
            return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), blockState2, relativeBlockInfo.nbt());
        }
    }
}
