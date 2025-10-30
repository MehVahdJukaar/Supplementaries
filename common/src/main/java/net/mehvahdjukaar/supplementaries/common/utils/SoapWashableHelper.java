package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoapWashableHelper {

    public static boolean canCleanColor(@NotNull Block block) {
        if (block.builtInRegistryHolder().is(ModTags.SOAP_BLACKLIST_BLOCK)) return false;
        return !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(block));
    }

    public static boolean canCleanColor(@NotNull Item item) {
        if (item.builtInRegistryHolder().is(ModTags.SOAP_BLACKLIST_ITEM)) return false;
        return !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(item));
    }

    //support: waxed, forge waxed, copper, IW stuff
    public static boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {

        if (tryWashWithInterface(level, pos, state, hitVec) ||
                tryCleaningSign(level, pos, state) ||
                tryChangingColor(level, pos, state) ||
                tryCleaningPiston(level, pos, state) ||
                tryCleanFromConfig(level, pos, state) ||
                tryUnWax(level, pos, state)
        ) {
            if (level instanceof ServerLevel serverLevel) {
                ModNetwork.CHANNEL.sendToAllClientPlayersInParticleRange(serverLevel, pos,
                        new ClientBoundParticlePacket(pos, ClientBoundParticlePacket.Type.BUBBLE_CLEAN));
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


    private static boolean tryUnWax(Level level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        BlockState toPlace = null;
        //vanilla
        Block unWaxed = HoneycombItem.WAXABLES.get().inverse().get(b);
        if (unWaxed != null) {
            toPlace = unWaxed.withPropertiesOf(state);
        }

        if (toPlace == null) {
            toPlace = tryParseWax(state);
        }

        BlockEntity oldBe = null;
        if (b instanceof EntityBlock) {
            oldBe = level.getBlockEntity(pos);
        }

        if (toPlace != null) {
            level.setBlock(pos, toPlace, 11);

            if (oldBe != null) {
                CompoundTag tag = oldBe.saveWithoutMetadata(level.registryAccess());
                var be = level.getBlockEntity(pos);
                if (be != null) {
                    be.loadWithComponents(tag, level.registryAccess());
                }
            }
            return true;
        }
        return false;
    }


    private static boolean tryCleaningPiston(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.STICKY_PISTON)) {
            Direction dir = state.getValue(PistonBaseBlock.FACING);
            BlockPos headPos = pos.relative(dir);
            BlockState headState = level.getBlockState(headPos);
            if (headState.is(Blocks.PISTON_HEAD) && headState.getValue(PistonHeadBlock.FACING) == dir) {
                level.setBlockAndUpdate(headPos, headState.setValue(PistonHeadBlock.TYPE, PistonType.DEFAULT));
            }
            level.setBlockAndUpdate(pos, Blocks.PISTON.withPropertiesOf(state));
            return true;
        } else if (state.is(Blocks.PISTON_HEAD) && state.getValue(PistonHeadBlock.TYPE) == PistonType.STICKY) {
            Direction dir = state.getValue(PistonHeadBlock.FACING);
            BlockPos basePos = pos.relative(dir.getOpposite());
            BlockState baseState = level.getBlockState(basePos);
            level.setBlockAndUpdate(pos, Blocks.PISTON_HEAD.withPropertiesOf(state).setValue(PistonHeadBlock.TYPE, PistonType.DEFAULT));
            if (baseState.is(Blocks.STICKY_PISTON) && baseState.getValue(PistonBaseBlock.FACING) == dir) {
                level.setBlockAndUpdate(basePos, Blocks.PISTON.withPropertiesOf(baseState));
            }
            return true;
        }
        return false;
    }


    private static boolean tryCleanFromConfig(Level level, BlockPos pos, BlockState state) {
        BlockState toPlace = null;
        for (var e : CommonConfigs.Functional.SOAP_SPECIAL.get().entrySet()) {
            if (e.getKey().test(state)) {
                toPlace = BuiltInRegistries.BLOCK.getOptional(e.getValue()).map(s -> s.withPropertiesOf(state)).orElse(null);
                break;
            }
        }
        if (toPlace != null) {
            level.setBlock(pos, toPlace, 11);
            return true;
        }
        return false;
    }


    private static BlockState tryParseWax(BlockState oldState) {
        ResourceLocation r = Utils.getID(oldState.getBlock());
        //hardcoding goes brr. This is needed, and I can't just use forge event since I only want to react to axe scrape, not stripping
        String name = r.getPath();
        String[] keywords = new String[]{"waxed_", "weathered_", "exposed_", "oxidized_",
                "_waxed", "_weathered", "_exposed", "_oxidized"};
        for (String key : keywords) {
            if (name.contains(key)) {
                String newName = name.replace(key, "");
                var bb = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(r.getNamespace(), newName));
                if (bb.isEmpty()) {
                    //tries minecraft namespace
                    bb = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(newName));
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
            cap = SuppPlatformStuff.getForgeCap(b, IWashable.class);
        }
        if (cap == null) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile != null) {
                if (tile instanceof IWashable soapWashable) {
                    cap = soapWashable;
                } else {
                    cap = SuppPlatformStuff.getForgeCap(tile, IWashable.class);
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
            if (state.hasProperty(BedBlock.PART)) {
                BlockPos other = pos.relative(BlockUtil.getConnectedBedDirection(state));
                BlockState otherBed = level.getBlockState(other);
                Block otherBedColor = BlocksColorAPI.changeColor(otherBed.getBlock(), null);
                if (otherBedColor != null) {
                    //level.removeBlock(other,false);
                    level.setBlock(other, otherBedColor.withPropertiesOf(otherBed), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
                }
            }

            BlockEntity oldBe = null;
            if (newColor instanceof EntityBlock) {
                oldBe = level.getBlockEntity(pos);
            }

            BlockState toPlace = newColor.withPropertiesOf(state);

            level.setBlock(pos, toPlace, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            if (oldBe != null) {
                CompoundTag tag = oldBe.saveWithoutMetadata();
                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    be.load(tag);
                }
            }
            return true;
        }
        return false;
    }
}

