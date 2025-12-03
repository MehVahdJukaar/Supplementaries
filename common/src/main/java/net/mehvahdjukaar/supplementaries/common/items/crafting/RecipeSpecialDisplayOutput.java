package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.function.Consumer;

public class RecipeSpecialDisplayOutput<T extends Recipe<?>> {

    private final RecipeManager manager;
    private final Consumer<List<RecipeHolder<T>>> consumer;

    private RecipeSpecialDisplayOutput(Consumer<List<RecipeHolder<T>>> consumer) {
        this.manager = Minecraft.getInstance().level.getRecipeManager();
        this.consumer = consumer;
    }

    public boolean add(String originalJson, List<RecipeHolder<? extends T>> recipes) {
        ResourceLocation id = originalJson.contains(":") ? ResourceLocation.tryParse(originalJson) : Supplementaries.res(originalJson);
        if (manager.byKey(id).isEmpty()) return false;
        consumer.accept((List) recipes);
        return true;
    }

    public static <A extends Recipe<?>> RecipeSpecialDisplayOutput<A> of(Consumer<List<RecipeHolder<A>>> consumer) {
        return new RecipeSpecialDisplayOutput<>(consumer);
    }

}
