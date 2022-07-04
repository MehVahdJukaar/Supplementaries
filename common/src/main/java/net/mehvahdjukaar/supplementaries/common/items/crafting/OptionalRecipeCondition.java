package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public record OptionalRecipeCondition(String condition) implements ICondition {

    private static final String CONDITION_NAME = "flag";
    public static final ResourceLocation ID = Supplementaries.res(CONDITION_NAME);

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return RegistryConfigs.Reg.isEnabled(this.condition);
    }

    public boolean test() {
        return RegistryConfigs.Reg.isEnabled(this.condition);
    }

    public static class Serializer implements IConditionSerializer<OptionalRecipeCondition> {

        public Serializer() {
        }

        @Override
        public void write(JsonObject json, OptionalRecipeCondition value) {
            json.addProperty(CONDITION_NAME, value.condition);
        }

        @Override
        public OptionalRecipeCondition read(JsonObject json) {
            return new OptionalRecipeCondition(json.getAsJsonPrimitive(CONDITION_NAME).getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
