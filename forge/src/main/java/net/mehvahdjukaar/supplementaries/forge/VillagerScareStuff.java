package net.mehvahdjukaar.supplementaries.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.entity.VillagerAIHooks;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


public class VillagerScareStuff {

    private static final Supplier<Activity> NOTEBLOCK_SCARE = RegHelper.registerActivity(Supplementaries.res("scared_by_noteblock"),
            () -> new Activity("scared_by_noteblock"));

    private static final Supplier<MemoryModuleType<BlockPos>> NEAREST_NOTEBLOCK = RegHelper.registerMemoryModule(
            Supplementaries.res("scary_noteblock"), () -> new MemoryModuleType<>(Optional.empty()) //not saved. thanks overengineered system
    );
    private static final int DESIRED_DISTANCE = 4;

    public static void init() {
        VillagerAIHooks.addBrainModification(VillagerScareStuff::modifyBrain);
    }

    public static void setup() {
        VillagerAIHooks.registerMemory(NEAREST_NOTEBLOCK.get());
    }

    private static void modifyBrain(IVillagerBrainEvent event) {
        event.addOrReplaceActivity(NOTEBLOCK_SCARE.get(), getNoteblockPanicPackage(0.5F));
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getNoteblockPanicPackage(float speedModifier) {
        float f = speedModifier * 1.5F;
        return ImmutableList.of(Pair.of(0, createCamlDownBehavior()),
                Pair.of(1, SetWalkTargetAwayFrom.pos(NEAREST_NOTEBLOCK.get(), f, DESIRED_DISTANCE, false)),
                Pair.of(3, VillageBoundRandomStroll.create(f, 2, 2)), getMinimalLookBehavior());
    }

    public static BehaviorControl<LivingEntity> createCamlDownBehavior() {
        return BehaviorBuilder.create((instance) -> instance.group(
                instance.registered(NEAREST_NOTEBLOCK.get())
        ).apply(instance, ((nearestNoteblock) -> (level, livingEntity, time) -> {
            boolean isFarAway = instance.tryGet(nearestNoteblock).filter((pos) ->
                    pos.distToCenterSqr(livingEntity.position()) <= DESIRED_DISTANCE * DESIRED_DISTANCE).isPresent();
            if (!isFarAway) {
                nearestNoteblock.erase();
                livingEntity.getBrain().updateActivityFromSchedule(level.getDayTime(), level.getGameTime());
            }
            return true;
        })));
    }

    private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))));
    }

    public static void scareVillagers(LevelAccessor level, BlockPos pos) {
        if (CommonConfigs.Tweaks.SCARE_VILLAGERS.get()) {
            var villagers = level.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(8));
            for (var v : villagers) {
                var brain = v.getBrain();
                if (!brain.isActive(Activity.PANIC) && !brain.isActive(NOTEBLOCK_SCARE.get())) {
                    brain.eraseMemory(MemoryModuleType.PATH);
                    brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                    brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
                    brain.eraseMemory(MemoryModuleType.BREED_TARGET);
                    brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
                    brain.setMemory(NEAREST_NOTEBLOCK.get(), pos);
                }
                brain.setActiveActivityIfPossible(NOTEBLOCK_SCARE.get());
            }
        }
    }
}
