package net.mehvahdjukaar.supplementaries.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobHeadShadersManager extends SimpleJsonResourceReloadListener {

    private static final PostShadersHelper.Group MOB_HEAD_SHADER_GROUP = new PostShadersHelper.Group(
            Supplementaries.res("mob_heads_shaders"), -20);

    public static final MobHeadShadersManager INSTANCE = new MobHeadShadersManager();

    private final Map<Item, ResourceLocation> effects = new HashMap<>();
    private final Map<EntityType<?>, ResourceLocation> entityEffects = new HashMap<>();

    public MobHeadShadersManager() {
        super(new Gson(), "mob_head_effects");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        effects.clear();
        entityEffects.clear();
        for (var entry : object.entrySet()) {
            //   RegistryOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, null, this);

            var effect = MobHeadEffect.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow();

            ResourceLocation shaderPath = effect.getShaderPath();
            for (Item item : effect.items) {
                if (item == Items.AIR)
                    throw new IllegalArgumentException("Mob head effect cannot have AIR item: " + effect.shader);
                effects.put(item, shaderPath);
            }
            for (EntityType<?> entityType : effect.entityTypes) {
                entityEffects.put(entityType, shaderPath);
            }
        }
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public String getShaderForItem(Item item) {
        return getShaderPathForItem(item).getPath();
    }

    @Nullable
    public ResourceLocation getShaderPathForItem(Item item) {
        return effects.get(item);
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public String getShaderForEntity(EntityType<?> entityType) {
        return getShaderPathForEntity(entityType).getPath();
    }

    @Nullable
    public ResourceLocation getShaderPathForEntity(EntityType<?> entityType) {
        return entityEffects.get(entityType);
    }


    public void applyMobHeadShaders(Player p, Minecraft mc) {
        if (!ClientConfigs.Tweaks.MOB_HEAD_EFFECTS.get()) return;
        ResourceLocation newShader = null;

        //no shaders in spectator
        if (!p.isSpectator()) {

            ItemStack stack = p.getItemBySlot(EquipmentSlot.HEAD);
            if (!CompatHandler.QUARK || !QuarkCompat.shouldHideOverlay(stack)) {

                Item item = stack.getItem();
                if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                    newShader = getShaderPathForItem(item);
                }

                if (newShader == null && shouldHaveGoatedEffect(p, item)) {
                    newShader = ClientRegistry.BARBARIC_RAGE_SHADER;
                }
            }
        }
        PostShadersHelper.toggleEffect(newShader, MOB_HEAD_SHADER_GROUP);
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
        return switch (entity) {
            case AbstractSkeleton ignored -> (ClientRegistry.BLACK_AND_WHITE_SHADER);
            case Zombie ignored -> (ClientRegistry.DESATURATE_SHADER);
            case Rabbit e when e.getVariant() == Rabbit.Variant.EVIL -> (ClientRegistry.RAGE_SHADER);
            case Piglin ignored -> (ClientRegistry.GLITTER_SHADER);
            case WitherBoss ignored -> (ClientRegistry.BLACK_AND_WHITE_SHADER);
            default -> null;
        };
    }

    //no holders since it loads on boot. lets keep it simple for once
    private record MobHeadEffect(List<Item> items, List<EntityType<?>> entityTypes, ResourceLocation shader) {

        public static final Codec<MobHeadEffect> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        MiscUtils.LENIENT_ITEM_OR_ITEM_LIST.lenientOptionalFieldOf("items", List.of()).
                                forGetter(MobHeadEffect::items),
                        MiscUtils.LENIENT_ENTITY_OR_ITEM_LIST.lenientOptionalFieldOf("entity_types", List.of()).
                                forGetter(MobHeadEffect::entityTypes),
                        ResourceLocation.CODEC.fieldOf("shader").forGetter(MobHeadEffect::shader)
                ).apply(instance, MobHeadEffect::new)
        );

        public ResourceLocation getShaderPath() {
            return shader.withPath(p -> "shaders/post/" + p + ".json");
        }

    }
}
