package net.mehvahdjukaar.supplementaries.configs;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
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
        //special double condition cases
        if(flag.equals("firefly_jar")){
            return RegistryConfigs.reg.FIREFLY_JAR;
        }
        return RegistryConfigs.reg.isEnabled(flag);
    }

    public static class Serializer implements IConditionSerializer<RecipeCondition> {
        private final ResourceLocation location;

        public Serializer(ResourceLocation location) {
            this.location = location;
        }

        @Override
        public void write(JsonObject json, RecipeCondition value) {
            json.addProperty("flag", value.flag);
        }

        @Override
        public RecipeCondition read(JsonObject json) {
            return new RecipeCondition(json.getAsJsonPrimitive("flag").getAsString(), location);
        }

        @Override
        public ResourceLocation getID() {
            return location;
        }
    }
}
