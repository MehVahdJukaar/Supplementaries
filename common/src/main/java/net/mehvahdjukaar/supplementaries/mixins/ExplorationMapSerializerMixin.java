package net.mehvahdjukaar.supplementaries.mixins;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.utils.IExplorationFunctionExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplorationMapFunction.Serializer.class)
public class ExplorationMapSerializerMixin {

    @Inject(at = @At("HEAD"),
            method = "serialize(Lcom/google/gson/JsonObject;Lnet/minecraft/world/level/storage/loot/functions/ExplorationMapFunction;Lcom/google/gson/JsonSerializationContext;)V")
    public void saveCustomDeco(JsonObject json, ExplorationMapFunction value, JsonSerializationContext serializationContext, CallbackInfo ci){
        if(value instanceof IExplorationFunctionExtension e && e.supplementaries$getCustomDecoration() != null){
            json.addProperty("custom_decoration", e.supplementaries$getCustomDecoration().toString());
        }
    }

    @ModifyReturnValue(at = @At("RETURN"),
            method = "deserialize(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;[Lnet/minecraft/world/level/storage/loot/predicates/LootItemCondition;)Lnet/minecraft/world/level/storage/loot/functions/ExplorationMapFunction;")
    public ExplorationMapFunction readCustomDeco(ExplorationMapFunction value, JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditions){
        if(value instanceof IExplorationFunctionExtension e){
            String string = object.has("custom_decoration") ? GsonHelper.getAsString(object, "custom_decoration") : null;
            if(string != null){
                e.supplementaries$setCustomDecoration(new ResourceLocation( string));
            }
        }
        return value;
    }
}
