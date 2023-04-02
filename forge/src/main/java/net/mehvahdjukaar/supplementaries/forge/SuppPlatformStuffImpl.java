package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.util.fake_player.FakePlayerManager;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.mixins.forge.MobBucketItemAccessor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class SuppPlatformStuffImpl {

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).invokeGetFishType();
    }

    /**
     * Does not check if its instance of ICapabilityProvider
     * Be sure to provide it with one, or it will fail
     */
    @Nullable
    public static <T> T getForgeCap(Object object, Class<T> capClass) {
        var t = CapabilityHandler.getToken(capClass);
        if (t != null && object instanceof ICapabilityProvider cp) {
            return CapabilityHandler.get(cp, t);
        }
        return null;
    }

    @Nullable
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        Player fp = FakePlayerManager.getDefault(level);
        fp.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
        Block b = state.getBlock();
        var context = new UseOnContext(fp, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        var modified = state;
        modified = b.getToolModifiedState(modified, context, ToolActions.AXE_WAX_OFF, false);
        if (modified == null) modified = state;
        while (true) {
            var newMod = b.getToolModifiedState(modified, context, ToolActions.AXE_SCRAPE, false);

            if (newMod == null || newMod == modified) break;
            else modified = newMod;
        }
        if (modified == state) return null;
        return modified;
    }

    public static boolean isEndermanMask(EnderMan enderMan, Player player, ItemStack itemstack) {
        try {
            return itemstack.isEnderMask(player, enderMan);
        } catch (Exception e) {
            return false;
        }
    }

    public static int getItemLifeSpawn(ItemEntity itemEntity) {
        return itemEntity.lifespan;
    }

    public static void onItemPickup(Player player, ItemEntity itemEntity, ItemStack copy) {
        ForgeEventFactory.firePlayerItemPickupEvent(player, itemEntity, copy);
    }

    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        return state.getLightEmission(level, pos);
    }

    public static RenderType staticNoise(ResourceLocation location) {
        return SupplementariesForgeClient.staticNoise(location);
    }
    public static ShaderInstance getNoiseShader() {
        return SupplementariesForgeClient.getStaticNoiseShader();
    }

}
