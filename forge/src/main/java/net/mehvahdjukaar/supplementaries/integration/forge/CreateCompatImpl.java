package net.mehvahdjukaar.supplementaries.integration.forge;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.entity.Entity;

import static com.simibubi.create.infrastructure.ponder.AllPonderTags.DISPLAY_SOURCES;
import static com.simibubi.create.infrastructure.ponder.AllPonderTags.DISPLAY_TARGETS;

public class CreateCompatImpl {
    public static boolean isContraption(MovementContext context, Entity passenger) {
        return passenger instanceof AbstractContraptionEntity ace
                && ace.getContraption() == context.contraption;
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
}
