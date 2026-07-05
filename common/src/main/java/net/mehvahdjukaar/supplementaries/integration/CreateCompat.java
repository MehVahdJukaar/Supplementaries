package net.mehvahdjukaar.supplementaries.integration;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.integration.create.*;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Create integration: display link sources/targets, contraption movement behaviours, the present-recipient
 * item attribute, and cannons aimed/fired while mounted on trains and contraptions.
 *
 * <p>Create 6 is multiloader, so nearly everything lives in common and references Create directly. The
 * exception is {@link com.simibubi.create.content.contraptions.AbstractContraptionEntity}: its supertype set
 * differs per loader (NeoForge's {@code IEntityWithComplexSpawn} vs the Fabric equivalent), so any method that
 * touches it is bridged through {@link PlatformImpl} into the platform {@code CreateCompatImpl}. This class is
 * only class-loaded when Create is present (see {@link CompatHandler#CREATE}). Genuinely client-only code is
 * split into {@link CreateClientCompat}; client-only overrides of Create methods are marked {@code @ClientOnly}.
 */
public class CreateCompat {

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

    public static void setup() {
        registerCannonBehaviours(ModRegistry.CANNON.get());
        registerExtraMovementBehaviours();
    }

    public static void init() {
    }

    public static void setupClient() {
        //TODO: ponder tags
    }

    public static void registerExtraMovementBehaviours() {
        try {
            MovementBehaviour.REGISTRY.register(ModRegistry.BAMBOO_SPIKES.get(), new BambooSpikesBehavior());
            MovementBehaviour.REGISTRY.register(ModRegistry.HOURGLASS.get(), new HourglassBehavior());
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: {}", String.valueOf(e));
        }
    }

    /**
     * Client-side callback from the contraption interaction behaviour: enter cannon maneuver mode when the
     * player sneak-uses a cannon block on a moving contraption.
     */
    public static boolean onContraptionInteractClient(@Nullable BlockEntity be, Entity contraption, BlockPos localPos,
                                                      boolean secondaryUse) {
        if (!secondaryUse || !(be instanceof CannonBlockTile cannon)) return false;
        CreateClientCompat.startControlling(cannon, contraption, localPos);
        return true;
    }

    // === helpers used by the movement behaviours / display targets (no AbstractContraptionEntity) ===

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

    // === platform bridge: everything below hard-casts to Create's AbstractContraptionEntity, whose supertype
    //     set is loader-specific, so it lives in the platform CreateCompatImpl ===

    @PlatformImpl
    public static void registerCannonBehaviours(Block cannon) {
        throw new AssertionError();
    }

    /** True if the entity is a Create contraption (AbstractContraptionEntity). */
    @PlatformImpl
    public static boolean isContraptionEntity(Entity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Vec3 contraptionPosToGlobalPos(Entity contraption, Vec3 localVec, float partialTicks) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Quaternionf getContraptionRotation(Entity contraption, float partialTicks) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Vec3 getContactPointMotion(Entity contraption, Vec3 worldPoint) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    public static BlockEntity getClientBlockEntity(Entity contraption, BlockPos localPos) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    public static Entity findContraption(Level level, UUID contraptionId) {
        throw new AssertionError();
    }

    /**
     * Bake a cannon's aim into the contraption's stored block NBT so it persists past the live render.
     */
    @PlatformImpl
    public static void persistCannonAim(Entity contraption, BlockPos localPos, Quaternionf localRot, byte firePower) {
        throw new AssertionError();
    }

    /**
     * Add a mode-selector scroll input to a display-link source config line. Bridged because Create's config
     * widgets ({@code ScrollInput}) sit on Catnip types that are not on the common compile classpath. Keys are
     * resolved as {@code create.<optionPrefix>.<option>} and {@code create.<titleKey>}.
     */
    @PlatformImpl
    public static void addDisplaySourceModeConfig(ModularGuiLineBuilder builder, int width, String configKey,
                                                  String titleKey, String optionPrefix, String... options) {
        throw new AssertionError();
    }
}
