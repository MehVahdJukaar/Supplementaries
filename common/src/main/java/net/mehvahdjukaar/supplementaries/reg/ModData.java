package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.IAttachmentType;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.data.LivingEntityTamable;
import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.common.misc.cooperative.CooperativePistonData;
import net.mehvahdjukaar.supplementaries.common.misc.cooperative.PulleyCooperation;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.minecraft.world.entity.LivingEntity;

import static net.mehvahdjukaar.supplementaries.Supplementaries.res;

public class ModData {


    public static final WorldSavedDataType<CooperativePistonData> COOPERATIVE_PISTONS = RegHelper.registerWorldSavedData(
            Supplementaries.res("cooperative_pistons"),
            CooperativePistonData::createFromLevel, () -> CooperativePistonData.CODEC, null,
            true);

    public static final WorldSavedDataType<PulleyCooperation> COOPERATIVE_PULLEYS = RegHelper.registerWorldSavedData(
            Supplementaries.res("cooperative_pulleys"),
            PulleyCooperation::createFromLevel, () -> PulleyCooperation.CODEC, null,
            true);


    public static final WorldSavedDataType<GlobeData> GLOBE_DATA = RegHelper.registerWorldSavedData(
            res("globe_data"), GlobeData::createFromLevel, () -> GlobeData.CODEC, () -> GlobeData.STREAM_CODEC
    );


    public static final IAttachmentType<SlimedData, LivingEntity> SLIMED_DATA = RegHelper.registerDataAttachment(
            res("slimed_data"),
            () -> RegHelper.AttachmentBuilder.create(SlimedData::new)
                    .syncWith(SlimedData.STREAM_CODEC)
                    .persistent(SlimedData.CODEC),
            LivingEntity.class
    );

    public static final IAttachmentType<LivingEntityTamable, LivingEntity> LIVING_TAMABLE = RegHelper.registerDataAttachment(
            res("living_tamable"),
            () -> RegHelper.AttachmentBuilder.create(LivingEntityTamable::new)
                    .persistent(LivingEntityTamable.CODEC),
            LivingEntity.class
    );


    public static void init() {
        //just to make sure its loaded
    }
}
