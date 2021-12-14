package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.network.ClientBoundSpawnBlockParticlePacket;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionResult;
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
    public SoapItem(Properties pProperties) {
        super(pProperties);
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
