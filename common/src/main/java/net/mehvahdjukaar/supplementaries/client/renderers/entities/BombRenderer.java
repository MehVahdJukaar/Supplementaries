package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BombRenderer extends ImprovedThrownItemRenderer<BombEntity> {

    private static final float BABY_SCALE = 0.55f;

    public BombRenderer(EntityRendererProvider.Context context) {
        super(context, 1);
    }

    @Override
    protected float getScale(BombEntity entity) {
        return entity.getBombType() == BombEntity.BombType.BLUE_BABY ? BABY_SCALE : this.scale;
    }
}
