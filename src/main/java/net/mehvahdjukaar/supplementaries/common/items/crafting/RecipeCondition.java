package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.configs.RegistryConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class RecipeCondition implements ICondition {

    private final ResourceLocation res;
    private final String flag;

    public RecipeCondition(String flag, ResourceLocation loc) {
        this.flag = flag;
        this.res = loc;
    }

    @Override
    public ResourceLocation getID() {
        return res;
    }

    @Override
    public boolean test() {
        return RegistryConfigs.reg.isEnabled(flag);
    }

    public static class Serializer implements IConditionSerializer<RecipeCondition> {
        private final ResourceLocation location;
        private final String name;

        public Serializer(String name) {
            this.location = Supplementaries.res(name);
            this.name = name;
        }

        @Override
        public void write(JsonObject json, RecipeCondition value) {
            json.addProperty(name, value.flag);
        }

        @Override
        public RecipeCondition read(JsonObject json) {
            return new RecipeCondition(json.getAsJsonPrimitive(name).getAsString(), location);
        }

        @Override
        public ResourceLocation getID() {
            return location;
        }
    }
}
