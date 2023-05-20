package net.mehvahdjukaar.supplementaries.common.block;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Outline extends SimpleJsonResourceReloadListener {

    public Outline() {
        super(new Gson(), "outline_rules");
    }

    public static record OutlineRule(Block self, RuleTest selfTest, Map<Direction, RuleTest> targets) {
    }

    public static final Codec<OutlineRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("self").forGetter(OutlineRule::self),
            RuleTest.CODEC.fieldOf("self_test").forGetter(OutlineRule::selfTest),
            Codec.simpleMap(Direction.CODEC, RuleTest.CODEC, StringRepresentable.keys(Direction.values())).fieldOf("targets").forGetter(OutlineRule::targets)
    ).apply(instance, OutlineRule::new));

    private static final Map<Block, List<OutlineRule>> RULES = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        RULES.clear();
        List<OutlineRule> temp = new ArrayList<>();
        jsons.forEach((key, json) -> {
            try {
                var result = CODEC.parse(JsonOps.INSTANCE, json);
                OutlineRule rule = result.getOrThrow(false, e -> Supplementaries.LOGGER.error("Failed to parse outline rule: {}", e));
                temp.add(rule);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for outline rule " + key);
            }
        });

        temp.forEach(t -> RULES.computeIfAbsent(t.self, g -> new ArrayList<>()).add(t));
    }

    private static final RandomSource rs = RandomSource.create();

    //mixin in LevelRenderer:renderHitOutline getShape call
    private static VoxelShape renderHitOutline(LevelReader level, VoxelShape original, PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        var r = RULES.get(b);
        if (r != null) {
            for (var v : r) {
                if (v.selfTest.test(state, rs)) {
                    for (var e : v.targets.entrySet()) {
                        BlockState facing = level.getBlockState(pos.relative(e.getKey()));
                        if (e.getValue().test(facing, rs)) {
                            VoxelShape otherShape = null;//todo: figure out recursiveness here
                            original = Shapes.or(original, otherShape);
                        }
                    }

                    break;
                }
            }
        }
        return original;// default behavior

    }

}
