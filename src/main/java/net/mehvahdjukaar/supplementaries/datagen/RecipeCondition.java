package net.mehvahdjukaar.supplementaries.datagen;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class RecipeCondition implements ICondition {

    public static final ResourceLocation MY_FLAG = new ResourceLocation(Supplementaries.MOD_ID, "flag");

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
