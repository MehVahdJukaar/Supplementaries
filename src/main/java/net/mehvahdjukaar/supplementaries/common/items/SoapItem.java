package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSpawnBlockParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class SoapItem extends Item {
    public static final FoodProperties SOAP_FOOD = (new FoodProperties.Builder())
            .nutrition(0).saturationMod(0.1F).alwaysEat().effect(
                    ()->new MobEffectInstance(MobEffects.POISON,120),1).build();;

    public SoapItem(Properties pProperties) {
        super(pProperties.food(SOAP_FOOD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!hasBeenEatenBefore(player, level)) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.canEat(true)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
    }

    public static boolean hasBeenEatenBefore(Player player, Level level){
        ResourceLocation res = new ResourceLocation("supplementaries", "husbandry/soap");
        if(level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Advancement a = serverLevel.getServer().getAdvancements().getAdvancement(res);
            if (a != null) {
                return serverPlayer.getAdvancements().getOrStartProgress(a).isDone();
            }
        }else if(player instanceof LocalPlayer localPlayer){
            var advancements = localPlayer.connection.getAdvancements();
            Advancement a = advancements.getAdvancements().get(res);
            return a != null;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (tryCleaning(context.getItemInHand(), level, context.getClickedPos()))
            return InteractionResult.sidedSuccess(level.isClientSide);

        return super.useOn(context);
    }


    public static boolean tryCleaning(ItemStack stack, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState oldState = state;
        Block b = state.getBlock();

        ItemStack temp = new ItemStack(Items.IRON_AXE);

        ResourceLocation r = state.getBlock().getRegistryName();
        //hardcoding goes brr. This is needed and I can't just use forge event since I only want to react to axe scrape, not stripping
        if(r.getNamespace().equals("quark")){
            String name = r.getPath();
            if(name.contains("waxed")){
                Block bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r.getNamespace(),
                        name.replace("waxed_","")));
                if(bb != null && bb != Blocks.AIR){
                    state = BlockUtils.copyProperties(state, bb.defaultBlockState());
                }
            }
        }

        Optional<BlockState> optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, temp, ToolActions.AXE_WAX_OFF));
        if (optional.isPresent()) {
            state = optional.get();
        }

        optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, temp, ToolActions.AXE_SCRAPE));
        while (optional.isPresent()) {
            state = optional.get();
            optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, temp, ToolActions.AXE_SCRAPE));
        }
        if (state != oldState) {
            if (level instanceof ServerLevel serverLevel) {
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state, 11);
                NetworkHandler.sendToAllInRangeClients(pos, serverLevel, 32, new ClientBoundSpawnBlockParticlePacket(pos, 0));

            } else {
                ParticleUtil.spawnParticlesOnBlockFaces(level, pos, ModRegistry.SUDS_PARTICLE.get(),
                        UniformInt.of(2, 4), 0.001f,0.01f, true);
            }
            stack.shrink(1);

            return true;
        }
        return false;
    }
}
