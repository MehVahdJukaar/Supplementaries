package net.mehvahdjukaar.supplementaries.common.misc;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SoapWashableHelper {

    //support: waxed, forge waxed, copper, IW stuff
    public static boolean tryWash(Level level, BlockPos pos, BlockState state) {

        if (tryWashWithInterface(level, pos, state) ||
                tryChangingColor(level, pos, state) ||
                tryUnoxidise(level, pos, state)) {
            if (level instanceof ServerLevel serverLevel) {
                NetworkHandler.CHANNEL.sendToAllClientPlayersInRange(serverLevel, pos, 64,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.EventType.BUBBLE_CLEAN));
            }
            return true;
        }

        return false;
    }

    private static boolean tryUnoxidise(Level level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        BlockState toPlace = null;
        if (b == Blocks.STICKY_PISTON) {
            toPlace = Blocks.PISTON.withPropertiesOf(state);
        } else {
            SuppPlatformStuff.getUnoxidised(level, pos, state);
        }
        //vanilla
        if (toPlace == null) {
            var unWaxed = HoneycombItem.WAXABLES.get().inverse().get(b);
            if (unWaxed == null) {
                unWaxed = b;
            }
            unWaxed = WeatheringCopper.getFirst(unWaxed);
            if (unWaxed != b) toPlace = unWaxed.withPropertiesOf(state);
        }

        if (toPlace == null) {
            toPlace = tryParse(state);
        }

        if (toPlace != null) {
            level.setBlock(pos, toPlace, 11);
            return true;
        }
        return false;
    }


    private static BlockState tryParse(BlockState oldState) {
        ResourceLocation r = Utils.getID(oldState.getBlock());
        //hardcoding goes brr. This is needed, and I can't just use forge event since I only want to react to axe scrape, not stripping
        String name = r.getPath();
        String[] keywords = new String[]{"waxed_", "weathered_", "exposed_", "oxidized_",
                "_waxed", "_weathered", "_exposed", "_oxidized"};
        for (String key : keywords) {
            if (name.contains(key)) {
                String newName = name.replace(key, "");
                var bb = Registry.BLOCK.getOptional(new ResourceLocation(r.getNamespace(), newName));
                if (bb.isEmpty()) {
                    //tries minecraft namespace
                    bb = Registry.BLOCK.getOptional(new ResourceLocation(newName));
                }
                if (bb.isPresent()) {
                    BlockState newState = bb.get().withPropertiesOf(oldState);
                    if (newState != oldState) return newState;
                }
            }
        }
        return null;
    }

    private static boolean tryWashWithInterface(Level level, BlockPos pos, BlockState state) {
        ISoapWashable cap;
        Block b = state.getBlock();
        if (b instanceof ISoapWashable soapWashable) {
            cap = soapWashable;
        } else {
            cap = SuppPlatformStuff.getForgeCap(b, ISoapWashable.class);
        }
        if (cap == null) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile != null) {
                if (tile instanceof ISoapWashable soapWashable) {
                    cap = soapWashable;
                } else {
                    cap = SuppPlatformStuff.getForgeCap(tile, ISoapWashable.class);
                }
            }
        }
        if (cap != null) {
            return cap.tryWash(level, pos, state);
        }
        return false;
    }


    private static boolean tryChangingColor(Level level, BlockPos pos, BlockState state) {

        Block newColor = BlocksColorAPI.changeColor(state.getBlock(), null);

        if (newColor != null) {
            if (CommonConfigs.Items.SOAP_DYE_CLEAN_BLACKLIST.get()
                    .contains(BlocksColorAPI.getKey(state.getBlock()))) return false;

            //TODO: add back
            if (state.getBlock() instanceof BedBlock) {
                if(true)return false;
                BlockPos other = pos.relative(BlockUtil.getConnectedBedDirection(state));
                BlockState otherBed = level.getBlockState(other);
                Block otherBedColor = BlocksColorAPI.changeColor(otherBed.getBlock(), null);
                if (otherBedColor != null) {
                    //level.removeBlock(other,false);
                    level.setBlock(other, otherBedColor.withPropertiesOf(otherBed), 2);
                }
            }

            CompoundTag tag = null;
            if (newColor instanceof EntityBlock) {
                var be = level.getBlockEntity(pos);
                if (be != null) {
                    tag = be.saveWithoutMetadata();
                }
            }

            BlockState toPlace = newColor.withPropertiesOf(state);

            level.setBlock(pos, toPlace, 2);
            if (tag != null) {
                var be = level.getBlockEntity(pos);
                if (be != null) {
                    be.load(tag);
                }
            }
            return true;
        }
        return false;
    }
}

