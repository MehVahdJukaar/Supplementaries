package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.mixins.LoomMenuMixin;
import net.mehvahdjukaar.supplementaries.mixins.LoomScreenFlagMixin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import vazkii.quark.mixin.client.LoomScreenMixin;

import java.util.function.Supplier;

@Mixin(MobBucketItem.class)
public interface MobBucketItemAccessor {

    @Invoker("getFishType")
    EntityType<?> invokeGetFishType();

}
