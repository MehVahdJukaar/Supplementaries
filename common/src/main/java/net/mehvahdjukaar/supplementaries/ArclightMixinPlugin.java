package net.mehvahdjukaar.supplementaries;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final  class ArclightMixinPlugin implements IMixinConfigPlugin {
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
        "net.mehvahdjukaar.supplementaries.mixins.CreeperArclightMixin", () -> {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode("io.izzel.arclight.common.mixin.core.world.entity.monster.CreeperMixin");
            } catch (Exception e) {
                return false;
            }

            return true;
        }, "net.mehvahdjukaar.supplementaries.mixins.CreeperMixin", () -> {
            try {
                MixinService.getService().getBytecodeProvider().getClassNode("io.izzel.arclight.common.mixin.core.world.entity.monster.CreeperMixin");
            } catch (Exception e) {
                return true;
            }

            return false;
        }
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    // Boilerplate

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
