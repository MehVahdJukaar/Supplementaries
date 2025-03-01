package net.mehvahdjukaar.supplementaries.integration.neoforge;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.neoforge.create.*;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CreateCompatImpl {

    public static final Supplier<ItemAttributeType> PRESENT_ATTRIBUTE = RegHelper.
            register(Supplementaries.res("present_recipient"), PresentRecipientAttribute.Type::new,
                    CreateRegistries.ITEM_ATTRIBUTE_TYPE);

    static {
        RegHelper.register(
                Supplementaries.res("notice_board_display_target"), () -> {
                    var obj = new NoticeBoardDisplayTarget();
                    DisplayTarget.BY_BLOCK_ENTITY.register(ModRegistry.NOTICE_BOARD_TILE.get(), obj);
                    return obj;
                },
                CreateRegistries.DISPLAY_TARGET);


        RegHelper.register(
                Supplementaries.res("text_holder_display_target"), () -> {
                    var obj = new TextHolderDisplayTarget();
                    DisplayTarget.BY_BLOCK_ENTITY.register(ModRegistry.WAY_SIGN_TILE.get(), obj);
                    DisplayTarget.BY_BLOCK_ENTITY.register(ModRegistry.DOORMAT_TILE.get(), obj);
                    return obj;
                },
                CreateRegistries.DISPLAY_TARGET);

        RegHelper.register(
                Supplementaries.res("speaker_block_display_target"), () -> {
                    var obj = new SpeakerBlockDisplayTarget();
                    DisplayTarget.BY_BLOCK_ENTITY.register(ModRegistry.SPEAKER_BLOCK_TILE.get(), obj);
                    return obj;
                },
                CreateRegistries.DISPLAY_TARGET);

        RegHelper.register(
                Supplementaries.res("blackboard_display_target"), () -> {
                    var obj = new BlackboardDisplayTarget();
                    DisplayTarget.BY_BLOCK_ENTITY.register(ModRegistry.BLACKBOARD_TILE.get(), obj);
                    return obj;
                },
                CreateRegistries.DISPLAY_TARGET);

        RegHelper.register(
                Supplementaries.res("globe_display_source"), () -> {
                    var obj = new GlobeDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.GLOBE_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);

        RegHelper.register(
                Supplementaries.res("notice_board_display_source"), () -> {
                    var obj = new NoticeBoardDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.NOTICE_BOARD_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);

        RegHelper.register(
                Supplementaries.res("clock_source"), () -> {
                    var obj = new ClockDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.CLOCK_BLOCK_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);

        RegHelper.register(
                Supplementaries.res("item_display_source"), () -> {
                    var obj = new ItemDisplayDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.PEDESTAL_TILE.get(), List.of(obj));
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.ITEM_SHELF_TILE.get(), List.of(obj));
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.STATUE_TILE.get(), List.of(obj));
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.HOURGLASS_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);

        RegHelper.register(
                Supplementaries.res("fluid_tank_source"), () -> {
                    var obj = new FluidFillLevelDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(ModRegistry.JAR_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);

    }

    public static void init() {

    }

    public static void setup() {
        try {
            MovementBehaviour.REGISTRY.register(ModRegistry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            MovementBehaviour.REGISTRY.register(ModRegistry.HOURGLASS.get(), new HourglassBehavior());


        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: {}", String.valueOf(e));
        }
    }


    public static void setupClient() {

        //TODO:
        /*
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.WAY_SIGN_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_TARGETS).add(ModRegistry.DOORMAT.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.SPEAKER_BLOCK.get());
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(ModRegistry.BLACKBOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.GLOBE_ITEM.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.PEDESTAL.get());
        PonderRegistry.TAGS.forTag(DISPLAY_SOURCES).add(ModRegistry.JAR.get());
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_SOURCES).add(ModRegistry.CLOCK_BLOCK.get());

         */
    }

    public static boolean isContraption(MovementContext context, Entity passenger) {
        return false;
        //TODO: add back
        //return passenger instanceof AbstractContraptionEntity ace
        //      && ace.getContraption() == context.contraption;
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
