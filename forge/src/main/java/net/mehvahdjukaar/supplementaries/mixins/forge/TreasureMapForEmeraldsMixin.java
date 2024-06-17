package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(targets = {"net.minecraft.world.entity.npc.VillagerTrades$TreasureMapForEmeralds"})
public abstract class TreasureMapForEmeraldsMixin {
    @Shadow
    @Final
    private TagKey<Structure> destination;

    @Final
    @Shadow
    private MapDecoration.Type destinationType;

    @Final
    @Shadow
    private int emeraldCost;
    @Final
    @Shadow
    private String displayName;
    @Final
    @Shadow
    private int maxUses;
    @Final
    @Shadow
    private int villagerXp;

    @Inject(method = "getOffer", at = @At("HEAD"), cancellable = true)
    public void turnToQuill(Entity trader, RandomSource random, CallbackInfoReturnable<MerchantOffer> cir) {
        if (trader.level() instanceof ServerLevel serverLevel) {
            if (CompatHandler.QUARK && CommonConfigs.Tweaks.REPLACE_VANILLA_MAPS.get()) {
                ResourceLocation decoration = new ResourceLocation(this.destinationType.toString().toLowerCase(Locale.ROOT));
                ItemStack map = QuarkCompat.makeAdventurerQuill(serverLevel, this.destination,
                        100, true, 2, decoration, null, 0);
                map.setHoverName(Component.translatable(this.displayName));
                int uses = 2;
                int xp = (int) ((this.villagerXp * this.maxUses) / (float) uses);
                int cost = (int) (this.emeraldCost * 1);
                cir.setReturnValue(new MerchantOffer(new ItemStack(Items.EMERALD, cost),
                        new ItemStack(Items.COMPASS), map, uses, xp, 0.2F));
            }
        }
    }
}