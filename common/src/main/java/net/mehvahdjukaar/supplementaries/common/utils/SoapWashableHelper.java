package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SoapWashableHelper {

    public static boolean canCleanColor(Block block) {
        if (block.builtInRegistryHolder().is(ModTags.SOAP_BLACKLIST_BLOCK)) return false;
        return !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(block));
    }

    public static boolean canCleanColor(Item item) {
        if (item.builtInRegistryHolder().is(ModTags.SOAP_BLACKLIST_ITEM)) return false;
        return !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(item));
    }

    //support: waxed, forge waxed, copper, IW stuff
    public static boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {

        if (tryWashWithInterface(level, pos, state, hitVec) ||
                tryCleaningSign(level, pos, state) ||
                tryChangingColor(level, pos, state) ||
                tryUnoxidise(level, pos, state)) {
            if (level instanceof ServerLevel serverLevel) {
                NetworkHelper.sendToAllClientPlayersInParticleRange(serverLevel, pos,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Kind.BUBBLE_CLEAN));
            }
            return true;
        }

        return false;
    }

    private static boolean tryCleaningSign(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof SignBlock) {
            if (level.getBlockEntity(pos) instanceof SignBlockEntity te && te.isWaxed()) {
                te.setWaxed(false);
                if (!level.isClientSide) {
                    te.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean tryUnoxidise(Level level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        BlockState toPlace = null;
        for (var e : CommonConfigs.Functional.SOAP_SPECIAL.get().entrySet()) {
            if (e.getKey().test(state)) {
                toPlace = BuiltInRegistries.BLOCK.getOptional(e.getValue()).map(s -> s.withPropertiesOf(state)).orElse(null);
                break;
            }
        }
        if (toPlace == null) {
            toPlace = SuppPlatformStuff.getUnoxidised(level, pos, state);
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
                var bb = BuiltInRegistries.BLOCK.getOptional(r.withPath(newName));
                if (bb.isEmpty()) {
                    //tries minecraft namespace
                    bb = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(newName));
                }
                if (bb.isPresent()) {
                    BlockState newState = bb.get().withPropertiesOf(oldState);
                    if (newState != oldState) return newState;
                }
            }
        }
        return null;
    }

    private static boolean tryWashWithInterface(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
        IWashable cap;
        Block b = state.getBlock();
        if (b instanceof IWashable soapWashable) {
            cap = soapWashable;
        } else {
            cap = SuppPlatformStuff.getForgeCap(level, pos, IWashable.class);
        }
        if (cap == null) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile != null) {
                if (tile instanceof IWashable soapWashable) {
                    cap = soapWashable;
                }
            }
        }
        if (cap != null) {
            return cap.tryWash(level, pos, state, hitVec);
        }
        return false;
    }


    private static boolean tryChangingColor(Level level, BlockPos pos, BlockState state) {

        Block newColor = BlocksColorAPI.changeColor(state.getBlock(), null);

        if (newColor != null) {
            if (!canCleanColor(state.getBlock())) return false;

            //TODO: add back
            if (state.getBlock() instanceof BedBlock) {
                if (true) return false;
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
                    tag = be.saveWithoutMetadata(level.registryAccess());
                }
            }

            BlockState toPlace = newColor.withPropertiesOf(state);

            level.setBlock(pos, toPlace, 2);
            if (tag != null) {
                var be = level.getBlockEntity(pos);
                if (be != null) {
                    be.loadWithComponents(tag, level.registryAccess());
                }
            }
            return true;
        }
        return false;
    }
}

