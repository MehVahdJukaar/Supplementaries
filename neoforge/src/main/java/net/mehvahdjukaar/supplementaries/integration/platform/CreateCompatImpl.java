package net.mehvahdjukaar.supplementaries.integration.platform;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Platform bridge for {@link CreateCompat}. Holds every reference to Create's
 * {@link AbstractContraptionEntity}, whose supertype set is loader-specific (NeoForge
 * {@code IEntityWithComplexSpawn} vs the Fabric equivalent) and therefore cannot be compiled in common.
 */
public class CreateCompatImpl {

    public static void registerCannonBehaviours(Block cannon) {
        MovingInteractionBehaviour.REGISTRY.register(cannon, new MovingInteractionBehaviour() {
            @Override
            public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
                                                   AbstractContraptionEntity contraptionEntity) {
                if (contraptionEntity.level().isClientSide) {
                    BlockEntity be = contraptionEntity.getContraption().getBlockEntityClientSide(localPos);
                    return CreateCompat.onContraptionInteractClient(be, contraptionEntity, localPos,
                            player.isSecondaryUseActive());
                }
                return player.isSecondaryUseActive();
            }
        });
    }

    public static boolean isContraptionEntity(Entity entity) {
        return entity instanceof AbstractContraptionEntity;
    }

    public static Vec3 contraptionPosToGlobalPos(Entity contraption, Vec3 localVec, float partialTicks) {
        return ((AbstractContraptionEntity) contraption).toGlobalVector(localVec, partialTicks);
    }

    public static Quaternionf getContraptionRotation(Entity contraption, float partialTicks) {
        AbstractContraptionEntity c = (AbstractContraptionEntity) contraption;
        Vec3 x = c.applyRotation(new Vec3(1, 0, 0), partialTicks);
        Vec3 y = c.applyRotation(new Vec3(0, 1, 0), partialTicks);
        Vec3 z = c.applyRotation(new Vec3(0, 0, 1), partialTicks);
        Matrix3f rot = new Matrix3f(
                new Vector3f((float) x.x, (float) x.y, (float) x.z),
                new Vector3f((float) y.x, (float) y.y, (float) y.z),
                new Vector3f((float) z.x, (float) z.y, (float) z.z));
        return new Quaternionf().setFromNormalized(rot);
    }

    public static Vec3 getContactPointMotion(Entity contraption, Vec3 worldPoint) {
        return ((AbstractContraptionEntity) contraption).getContactPointMotion(worldPoint);
    }

    @Nullable
    public static BlockEntity getClientBlockEntity(Entity contraption, BlockPos localPos) {
        return ((AbstractContraptionEntity) contraption).getContraption().getBlockEntityClientSide(localPos);
    }

    @Nullable
    public static Entity findContraption(Level level, UUID contraptionId) {
        if (level instanceof ServerLevel sl) {
            return sl.getEntity(contraptionId) instanceof AbstractContraptionEntity ce && !ce.isRemoved() ? ce : null;
        }
        if (level instanceof ClientLevel cl) {
            for (Entity e : cl.entitiesForRendering()) {
                if (e instanceof AbstractContraptionEntity ce && !ce.isRemoved() && ce.getUUID().equals(contraptionId)) {
                    return e;
                }
            }
        }
        return null;
    }

    public static void persistCannonAim(Entity contraption, BlockPos localPos, Quaternionf localRot, byte firePower) {
        Contraption c = ((AbstractContraptionEntity) contraption).getContraption();
        StructureTemplate.StructureBlockInfo info = c.getBlocks().get(localPos);
        if (info == null) return;
        CompoundTag nbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
        CannonBlockTile.buildAimNbt(nbt, info.state(), localRot, firePower);
        c.getBlocks().put(localPos, new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), nbt));
    }

    public static void addDisplaySourceModeConfig(ModularGuiLineBuilder builder, int width, String configKey,
                                                  String titleKey, String optionPrefix, String... options) {
        List<MutableComponent> opts = new ArrayList<>();
        for (String o : options) opts.add(Component.translatable("create." + optionPrefix + "." + o));
        MutableComponent title = Component.translatable("create." + titleKey);
        builder.addSelectionScrollInput(0, width, (si, l) -> si.forOptions(opts).titled(title), configKey);
    }
}
