package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.EnhancedLanternBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.world.generation.structure.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.world.generation.structure.StructureLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

public class BlockGeneratorBlockTile extends BlockEntity {

    private boolean firstTick = true;
    public Pair<List<Pair<Integer, BlockPos>>, Boolean> threadResult = null;

    public BlockGeneratorBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLOCK_GENERATOR_TILE.get(), pos, state);
    }

    private static final BlockState trapdoor = Blocks.SPRUCE_TRAPDOOR.defaultBlockState();
    private static final BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    private static final BlockState lanternDown = Blocks.LANTERN.defaultBlockState();
    private static final BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState jar = lantern;//ModRegistry.JAR.get().defaultBlockState(); //TODO: replace with new firefly jar
    private static final BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState log = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState stoneSlab = Blocks.STONE_SLAB.defaultBlockState();
    private static final BlockState stone = Blocks.STONE.defaultBlockState();
    private static final BlockState stair = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState air = Blocks.AIR.defaultBlockState();
    private static final BlockState path = Blocks.DIRT_PATH.defaultBlockState();
    private static final BlockState path_2 = Blocks.SMOOTH_SANDSTONE.defaultBlockState();


    private double averageAngles(float a, float b) {
        a = (float) (a * Math.PI / 180);
        b = (float) (b * Math.PI / 180);

        return (180 / Math.PI) * Mth.atan2(Mth.sin(a) + Mth.sin(b), Mth.cos(a) + Mth.cos(b));
    }

    //TODO: cleanup
    //TODO: this has to be the worst code I've written here
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BlockGeneratorBlockTile tile) {
        //if you are reading this I'm sorry...
        if (pLevel == null || pLevel.isClientSide) return;

        if (tile.firstTick) {
            tile.firstTick = false;

            ServerLevel world = (ServerLevel) pLevel;
            BlockPos pos = pPos.below(2);
            final int posX = pos.getX();
            final int posZ = pos.getZ();


            //lets hope world is thread safe
            try {
                Executors.newSingleThreadExecutor()
                        .submit(() -> tile.threadResult = StructureLocator.find(world, posX, posZ, 2));
            } catch (Exception e) {
                tile.failAndRemove(pLevel, pPos, e);
            }


            /*
            Thread thread = new Thread(() -> {
                try {
                    threadResult = StructureLocator.find(world, posX, posZ, 2);
                } catch (Exception e) {
                    this.level.removeBlock(this.worldPosition, false);
                    Supplementaries.LOGGER.warn("failed to generate road sign at " + this.worldPosition.toString() + ": " + e);
                }
            });
            thread.start();
            */

        }

        try {
            if (tile.threadResult != null) {

                ServerLevel world = (ServerLevel) pLevel;
                BlockPos pos = pPos.below(2);

                BlockState topState = trapdoor;

                Pair<List<Pair<Integer, BlockPos>>, Boolean> locateResult = tile.threadResult;

                List<Pair<Integer, BlockPos>> villages = locateResult.getLeft();

                //if I am in a village
                boolean inVillage = locateResult.getRight();

                if (inVillage) {
                    ResourceKey<Biome> b = ResourceKey.create(ForgeRegistries.Keys.BIOMES, world.getBiome(pos).getRegistryName());
                    BlockState replace = (b == Biomes.DESERT) ? path_2 : path;
                    replaceCobbleWithPath(world, pos, replace);
                }


                if (villages.size() >= 1) {


                    Random rand = world.random;
                    //if two signs will spawn
                    boolean twoSigns = true;
                    BlockPos village1;
                    BlockPos village2;
                    int dist1;
                    int dist2;


                    //only 1 sing found/ 1 sign post. always to closest village. posts that are relatively close to a village will always have two.
                    //posts in a village will point away
                    if (villages.size() == 1 || (0.3 > rand.nextFloat() && villages.get(0).getLeft() > 192)) {
                        dist1 = villages.get(0).getLeft();
                        village1 = villages.get(0).getRight();
                        dist2 = dist1;
                        village2 = village1;
                        twoSigns = false;
                    } else {
                        boolean inv = rand.nextBoolean();
                        dist1 = villages.get(inv ? 0 : 1).getLeft();
                        village1 = villages.get(inv ? 0 : 1).getRight();
                        dist2 = villages.get(inv ? 1 : 0).getLeft();
                        village2 = villages.get(inv ? 1 : 0).getRight();
                    }


                    world.setBlock(pos, ModRegistry.SIGN_POST.get().defaultBlockState(), 3);
                    if (world.getBlockEntity(pos) instanceof SignPostBlockTile sign) {
                        sign.setHeldBlock(Blocks.SPRUCE_FENCE.defaultBlockState());


                        boolean left = rand.nextBoolean();

                        sign.up = true;
                        sign.leftUp = left;
                        sign.pointToward(village1, true);


                        sign.down = twoSigns;
                        sign.leftDown = left;
                        sign.pointToward(village2, false);
                        if (Math.abs(sign.yawUp - sign.yawDown) > 90) {
                            sign.leftDown = !sign.leftDown;
                            sign.pointToward(village2, false);
                        }


                        if (ServerConfigs.cached.DISTANCE_TEXT) {
                            sign.textHolder.setLine(0, getSignText(dist1));
                            if (twoSigns)
                                sign.textHolder.setLine(1, getSignText(dist2));
                        }


                        float yaw = Mth.wrapDegrees(90 + (float) tile.averageAngles(-sign.yawUp + 180, -sign.yawDown + 180));
                        Direction backDir = Direction.fromYRot(yaw);

                        float diff = Mth.degreesDifference(yaw, backDir.toYRot());

                        Direction sideDir = (diff < 0 ? backDir.getClockWise() : backDir.getCounterClockWise());

                        ArrayList<Direction> lampDir = new ArrayList<>();
                        //lampDir.remove(sideDir);
                        lampDir.add(backDir.getOpposite());
                        lampDir.add(backDir.getOpposite());
                        lampDir.add(backDir.getOpposite());
                        if (Math.abs(diff) > 30) {
                            lampDir.add(sideDir.getOpposite());
                        }

                        boolean hasGroundLantern = false;

                        ResourceKey<Biome> biome = ResourceKey.create(ForgeRegistries.Keys.BIOMES, world.getBiome(pos).getRegistryName());
                        boolean hasFirefly = (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MAGICAL) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY) ? 0.2f : 0.01f) > rand.nextFloat();


                        //stone
                        if (0.3 > rand.nextFloat() && Mth.degreesDifferenceAbs(sign.getPointingYaw(true) + 180, yaw) > 70) {
                            BlockPos stonePos = pos.below().offset(backDir.getNormal());
                            if (rand.nextBoolean()) {
                                world.setBlock(stonePos, stoneSlab, 2);
                            } else {
                                world.setBlock(stonePos, stair.setValue(StairBlock.FACING, sideDir), 2);
                            }
                            stonePos = stonePos.offset(sideDir.getNormal());
                            world.setBlock(stonePos, stone, 2);
                            if (0.35 > rand.nextFloat()) {
                                world.setBlock(stonePos.above(), hasFirefly ? jar : lanternDown, 3);
                                hasGroundLantern = true;
                            }
                            stonePos = stonePos.offset(sideDir.getNormal());
                            if (!RoadSignFeature.isNotSolid(world, stonePos.below())) {
                                if (rand.nextBoolean()) {
                                    world.setBlock(stonePos, stoneSlab, 2);
                                } else {
                                    world.setBlock(stonePos, stair.setValue(StairBlock.FACING, sideDir.getOpposite()), 2);
                                }
                            }

                        }


                        if (!hasGroundLantern) {

                            //lanterns
                            pos = pos.above(2);

                            BlockState light = hasFirefly ? jar : lantern;

                            Direction dir = lampDir.get(rand.nextInt(lampDir.size()));

                            boolean doubleSided = 0.25 > rand.nextFloat();
                            if (doubleSided) {
                                dir = dir.getClockWise();
                            }

                            //wall lanterns
                            if (0.32 > rand.nextFloat()) {
                                topState = 0.32 > rand.nextFloat() ? trapdoor : air;

                                EnhancedLanternBlock wl = ((EnhancedLanternBlock) ModRegistry.WALL_LANTERN.get());
                                wl.placeOn(lanternDown, pos.below(), dir, world);

                                //double
                                if (doubleSided) {
                                    wl.placeOn(lanternDown, pos.below(), dir.getOpposite(), world);
                                }

                            } else {
                                boolean isTrapdoor = 0.4 > rand.nextFloat();

                                if (!isTrapdoor) topState = fence;

                                //double
                                if (doubleSided) {
                                    BlockPos backPos = pos.relative(dir.getOpposite());

                                    world.setBlock(backPos, isTrapdoor ? trapdoor : fence, 2);

                                    if (0.25 > rand.nextFloat()) {
                                        topState = isTrapdoor ? slab : log;
                                    }

                                    world.setBlock(backPos.below(), light, 3);
                                }

                                pos = pos.relative(dir);
                                BlockState frontState = isTrapdoor ? trapdoor : fence;
                                world.setBlock(pos, frontState, 2);

                                world.setBlock(pos.below(), light, 3);
                            }


                        }


                    }
                } else {

                    ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
                    CompoundTag com = new CompoundTag();
                    ListTag listTag = new ListTag();
                    listTag.add(StringTag.valueOf("nothing here but monsters\n\n\n"));
                    com.put("pages", listTag);
                    book.setTag(com);
                    BlockPos belowPos = pPos.below(2);
                    world.setBlock(belowPos, ModRegistry.NOTICE_BOARD.get().defaultBlockState().setValue(NoticeBoardBlock.HAS_BOOK, true)
                            .setValue(NoticeBoardBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(world.random)), 3);
                    if (world.getBlockEntity(belowPos) instanceof NoticeBoardBlockTile board) {
                        board.setDisplayedItem(book);
                        //te.setChanged();
                    }
                }

                world.setBlock(pPos, topState, 3);
            }

        } catch (Exception exception) {
            tile.failAndRemove(pLevel, pPos, exception);
        }
    }

    private void failAndRemove(Level level, BlockPos pos, Exception e) {
        level.removeBlock(pos, false);
        Supplementaries.LOGGER.warn("failed to generate road sign at " + pos + ": " + e);
    }

    private static Component getSignText(int d) {
        int s;
        if (d < 100) s = 10;
        else if (d < 2000) s = 100;
        else s = 1000;
        return new TranslatableComponent("message.supplementaries.road_sign", (((d + (s / 2)) / s) * s));
    }

    private static void replaceCobbleWithPath(Level world, BlockPos pos, BlockState path) {
        //generate cobble path

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                if (i == 0 && j == 0) continue;
                BlockPos pathPos = pos.offset(i, -2, j);
                BlockState state = world.getBlockState(pathPos);
                if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)) {
                    world.setBlock(pathPos, path, 2);
                }
            }
        }
    }

}
