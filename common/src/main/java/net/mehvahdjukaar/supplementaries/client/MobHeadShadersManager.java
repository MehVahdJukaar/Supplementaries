package net.mehvahdjukaar.supplementaries.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MobHeadShadersManager extends SimpleJsonResourceReloadListener {

    public static final MobHeadShadersManager INSTANCE = new MobHeadShadersManager();

    private final Map<Item, String> effects = new HashMap<>();
    private final Map<EntityType<?>, String> entityEffects = new HashMap<>();

    private final Set<String> myShaders = new HashSet<>();

    private String lastAppliedShader = null;

    public MobHeadShadersManager() {
        super(new Gson(), "mob_head_effects");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        effects.clear();
        entityEffects.clear();
        myShaders.clear();
        for (var entry : object.entrySet()) {
            var effect = MobHeadEffect.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(
                    false, error -> Supplementaries.LOGGER.error("Failed to parse mob head effect {}: {}", entry.getKey(), error)
            );
            String shaderPath = effect.getShaderPath();
            for (Item item : effect.items) {
                if (item == Items.AIR)
                    throw new IllegalArgumentException("Mob head effect cannot have AIR item: " + effect.shader);
                effects.put(item, shaderPath);
            }
            for (EntityType<?> entityType : effect.entityTypes) {
                entityEffects.put(entityType, shaderPath);
            }
            myShaders.add(shaderPath);
        }
        myShaders.add(ClientRegistry.BARBARIC_RAGE_SHADER);
    }

    @Nullable
    public String getShaderForItem(Item item) {
        return effects.get(item);
    }

    @Nullable
    public String getShaderForEntity(EntityType<?> entityType) {
        return entityEffects.get(entityType);
    }


    public void applyMobHeadShaders(Player p, Minecraft mc) {
        if (ClientConfigs.Tweaks.MOB_HEAD_EFFECTS.get()) {
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;

            String rendererShader = renderer.postEffect == null ? null : renderer.postEffect.getName();

            if (rendererShader != null && !myShaders.contains(rendererShader)) {
                return;
            }

            //no shaders in spectator
            if (p.isSpectator()) {
                if (rendererShader != null && lastAppliedShader != null) {
                    renderer.shutdownEffect();
                    lastAppliedShader = null;
                }
                return;
            }

            if (rendererShader == null && lastAppliedShader != null) {
                lastAppliedShader = null; //clear when something else unsets it
            }

            ItemStack stack = p.getItemBySlot(EquipmentSlot.HEAD);
            if (CompatHandler.QUARK && QuarkCompat.shouldHideOverlay(stack)) return;

            Item item = stack.getItem();
            String newShader;
            if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                newShader = getShaderForItem(item);
            } else newShader = null;

            if (newShader == null && shouldHaveGoatedEffect(p, item)) {
                newShader = ClientRegistry.BARBARIC_RAGE_SHADER;
            }
            if (newShader != null && (!newShader.equals(rendererShader) || !renderer.effectActive)) {
                renderer.loadEffect(ResourceLocation.tryParse(newShader));
                lastAppliedShader = newShader;
            } else if (rendererShader != null && newShader == null) {
                //remove my effect
                renderer.shutdownEffect();
                lastAppliedShader = null;
            }
        }
    }

    private boolean shouldHaveGoatedEffect(Player p, Item item) {
        return CompatHandler.GOATED && item == CompatObjects.BARBARIC_HELMET.get() && p.getHealth() < 5;
    }

    @Nullable
    public ResourceLocation getSpectatorShaders(Entity entity) {
        if (entity == null) return null;

        var s = getShaderForEntity(entity.getType());
        if (s != null) {
            return ResourceLocation.tryParse(s);
        }
        //hardcoded ones. Instance check is more compatible
        if (entity instanceof AbstractSkeleton) {
            return (ClientRegistry.BLACK_AND_WHITE_SHADER);
        } else if (entity instanceof Zombie) {
            return (ClientRegistry.VANILLA_DESATURATE_SHADER);
        } else if (entity instanceof Rabbit e && e.getVariant() == Rabbit.Variant.EVIL) {
            return (ClientRegistry.RAGE_SHADER);
        } else if (entity instanceof Piglin) {
            return (ClientRegistry.GLITTER_SHADER);
        } else if (entity instanceof WitherBoss) {
            return (ClientRegistry.BLACK_AND_WHITE_SHADER);
        }
        return null;
    }

    //no holders since it loads on boot. lets keep it simple for once
    private record MobHeadEffect(List<Item> items, List<EntityType<?>> entityTypes, ResourceLocation shader) {

        public static final Codec<MobHeadEffect> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("items", List.of()).
                                forGetter(MobHeadEffect::items),
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().optionalFieldOf("entity_types", List.of()).
                                forGetter(MobHeadEffect::entityTypes),
                        ResourceLocation.CODEC.fieldOf("shader").forGetter(MobHeadEffect::shader)
                ).apply(instance, MobHeadEffect::new)
        );

        public String getShaderPath() {
            return shader.withPath(p -> "shaders/post/" + p + ".json").toString();
        }

    }
}
