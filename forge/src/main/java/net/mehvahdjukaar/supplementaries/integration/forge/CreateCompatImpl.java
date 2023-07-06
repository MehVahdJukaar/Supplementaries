package net.mehvahdjukaar.supplementaries.integration.forge;


import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.forge.create.*;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.simibubi.create.infrastructure.ponder.AllPonderTags.DISPLAY_SOURCES;
import static com.simibubi.create.infrastructure.ponder.AllPonderTags.DISPLAY_TARGETS;

public class CreateCompatImpl {

    public static void setup() {
        try {
            AllMovementBehaviours.registerBehaviour(ModRegistry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            AllMovementBehaviours.registerBehaviour(ModRegistry.HOURGLASS.get(), new HourglassBehavior());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("notice_board_display_target"),
                    new NoticeBoardDisplayTarget()), ModRegistry.NOTICE_BOARD_TILE.get());

            DisplayBehaviour textHolderTarget = AllDisplayBehaviours.register(
                    Supplementaries.res("text_holder_display_target"), new TextHolderDisplayTarget());

            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.SIGN_POST_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.DOORMAT_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.DOORMAT_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("speaker_block_display_target"),
                    new SpeakerBlockDisplayTarget()), ModRegistry.SPEAKER_BLOCK_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("blackboard_display_target"),
                    new BlackboardDisplayTarget()), ModRegistry.BLACKBOARD_TILE.get());

            //sources
            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("globe_display_source"),
                    new GlobeDisplaySource()), ModRegistry.GLOBE_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("notice_board_display_source"),
                    new NoticeBoardDisplaySource()), ModRegistry.NOTICE_BOARD_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("clock_source"),
                    new ClockDisplaySource()), ModRegistry.CLOCK_BLOCK_TILE.get());

            DisplayBehaviour itemDisplaySource = AllDisplayBehaviours.register(
                    Supplementaries.res("item_display_source"),
                    new ItemDisplayDisplaySource());

            AllDisplayBehaviours.assignBlock(itemDisplaySource, ModRegistry.PEDESTAL.get());
            AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, ModRegistry.ITEM_SHELF_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, ModRegistry.STATUE_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, ModRegistry.HOURGLASS_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("fluid_tank_source"),
                    new FluidFillLevelDisplaySource()), ModRegistry.JAR_TILE.get());

        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: " + e);
        }
    }

    public static void setupClient() {
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_TARGETS).add(ModRegistry.DOORMAT.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.SPEAKER_BLOCK.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.BLACKBOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.GLOBE_ITEM.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.PEDESTAL.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.JAR.get());
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_SOURCES).add(ModRegistry.CLOCK_BLOCK.get());


    }



    public static Rotation isClockWise(UnaryOperator<Vec3> rot, Direction dir) {
        Vec3 v = MthUtils.V3itoV3(dir.getNormal());
        Vec3 v2 = rot.apply(v);
        var dot = v2.dot(new Vec3(0, 1, 0)); //??
        if (dot > 0) return Rotation.CLOCKWISE_90;
        else if (dot < 0) return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.NONE;
    }


    public static ItemStack getDisplayedItem(DisplayLinkContext context, BlockEntity source,
                                             Predicate<ItemStack> predicate) {
        if (source instanceof ItemDisplayTile display) {
            var stack = display.getDisplayedItem();
            if (predicate.test(stack)) return stack;
        } else {
            for (int i = 0; i < 32; ++i) {
                var pos = context.getSourcePos();
                TransportedItemStackHandlerBehaviour behaviour = BlockEntityBehaviour.get(
                        context.level(), pos, TransportedItemStackHandlerBehaviour.TYPE
                );
                if (behaviour == null) {
                    break;
                }
                MutableObject<ItemStack> stackHolder = new MutableObject<>();
                behaviour.handleCenteredProcessingOnAllItems(0.25F, tis -> {
                    stackHolder.setValue(tis.stack);
                    return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
                });
                ItemStack stack = stackHolder.getValue();
                if (stack != null && predicate.test(stack)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void changeState(MovementContext context, BlockState newState) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = context.contraption.getBlocks();
        if (blocks.containsKey(context.localPos)) {
            context.state = newState;
            StructureTemplate.StructureBlockInfo info = blocks.get(context.localPos);
            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt());
            blocks.replace(context.localPos, newInfo);
        }
    }

}
