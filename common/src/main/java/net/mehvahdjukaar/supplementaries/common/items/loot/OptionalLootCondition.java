package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import javax.annotation.Nonnull;

public record OptionalLootCondition(String flag) implements LootItemCondition {
    @Override
    public boolean test(LootContext lootContext) {
        return RegistryConfigs.isEnabled(this.flag);
    }

    @Nonnull
    @Override
    public LootItemConditionType getType() {
        return ModRegistry.OPTIONAL_LOOT_CONDITION.get();
    }

    public record FlagSerializer() implements Serializer<OptionalLootCondition> {
        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull OptionalLootCondition value, @Nonnull JsonSerializationContext context) {
            json.addProperty("flag", value.flag);
        }

        @Nonnull
        @Override
        public OptionalLootCondition deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
            String flag = json.getAsJsonPrimitive("flag").getAsString();
            return new OptionalLootCondition(flag);
        }
    }
}
