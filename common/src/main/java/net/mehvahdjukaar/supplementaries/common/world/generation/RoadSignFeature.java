package net.mehvahdjukaar.supplementaries.common.world.generation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;


public class RoadSignFeature extends Feature<NoneFeatureConfiguration> {

    public RoadSignFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }


    private static final BlockState COBBLE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState MOSSY_COBBLE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState FENCE = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState WALL = Blocks.COBBLESTONE_WALL.defaultBlockState();
    private static final BlockState MOSSY_WALL = Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState();
    private static final BlockState TRAPDOOR = Blocks.SPRUCE_TRAPDOOR.defaultBlockState();
    private static final BlockState LANTERN = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    private static final BlockState LANTERN_DOWN = Blocks.LANTERN.defaultBlockState();
    private static final BlockState JAR = LANTERN;//ModRegistry.JAR.get().defaultBlockState(); //TODO: replace with new firefly jar
    private static final BlockState SLAB = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState STONE_SLAB = Blocks.STONE_SLAB.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState STAIR = Blocks.STONE_STAIRS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState PATH = Blocks.DIRT_PATH.defaultBlockState();
    private static final BlockState SANDSTONE_PATH = Blocks.SMOOTH_SANDSTONE.defaultBlockState();


    public static boolean isNotSolid(LevelAccessor world, BlockPos pos) {
        return !world.isStateAtPosition(pos, (state) -> state.isRedstoneConductor(world, pos));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {

        WorldGenLevel reader = pContext.level();
        RandomSource rand = pContext.random();
        BlockPos pos = pContext.origin();

        /*
        if(!reader.getLevel().dimension().equals(World.OVERWORLD))return false;
        if(pos.getY()>90 || pos.getY()<50)return false;
        if(!reader.getLevel().getChunkSource().generator.getBiomeSource().canGenerateStructure(Structure.VILLAGE))return false;


        //find nearest solid block
        for(pos = pos.above(); canGoThrough(reader,pos) && pos.getY() > 2; pos = pos.below()) {}

        if(isNotSolid(reader, pos))return false;


        for(int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                //checks for empty blocks around wall
                for(int h = 2; h<=5; h++) {
                    //skip angles
                    //if(Math.abs(i)==2&&Math.abs(j)==2)continue;
                    if (!isReplaceable(reader,pos.offset(i,h,j))) {
                        return false;
                    }
                }
                //allows 1 block of leaves at the base
                if (!canGoThrough(reader,pos.offset(i,1,j)))return false;
                //thick solid base. no floaty sings here
                if(isNotSolid(reader, pos.offset(i, 0, j)))return false;
                if(isNotSolid(reader, pos.offset(i, -1, j)))return false;
                //if(isNotSolid(reader, pos.offset(i, -2, j)))return false;
            }
        }*/

        //for jigsaw
        pos = pos.below();

        //add air blocks around
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                for (int k = 1; k <= 4; ++k) {
                    if ((Math.abs(i) == 2 || Math.abs(j) == 2) && k == 1) continue;
                    reader.setBlock(pos.offset(i, k, j), ModRegistry.STRUCTURE_TEMP.get().defaultBlockState(), 2);
                }
            }
        }

        float humidity = reader.getBiome(pos).value().getDownfall();


        //generate cobble path
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                reader.setBlock(pos.offset(i, -1, j), COBBLE, 2);

                BlockPos pathPos = pos.offset(i, 0, j);
                double dist = pos.distToCenterSqr(pathPos.getX(), pathPos.getY(), pathPos.getZ()) / 5.2f;

                if (rand.nextFloat() < dist - 0.15) continue;
                boolean m = (humidity * 0.75) > rand.nextFloat();
                reader.setBlock(pathPos, m ? MOSSY_COBBLE : COBBLE, 2);
            }
        }

        //post

        boolean m = (humidity * 0.75) > rand.nextFloat();

        pos = pos.above();
        reader.setBlock(pos, m ? MOSSY_WALL : WALL, 2);
        pos = pos.above();
        reader.setBlock(pos, FENCE, 2);
        pos = pos.above();
        reader.setBlock(pos, FENCE, 2);
        reader.setBlock(pos.above(), ModRegistry.BLOCK_GENERATOR.get().defaultBlockState(), 2);


        return true;
    }


    //post process

    public static void applyPostProcess(ServerLevel level, BlockPos generatorPos, List<Pair<BlockPos, Holder<Structure>>> foundVillages) {

        BlockPos pos = generatorPos.below(2);

        BlockState topState = TRAPDOOR;

        List<Pair<Integer, BlockPos>> villages = new ArrayList<>();
        for (var r : foundVillages) {
            villages.add(Pair.of((int) Mth.sqrt((float) r.getFirst().distToCenterSqr(pos.getX(), pos.getY(), pos.getZ())), r.getFirst()));
        }

        //if I am in a village
        boolean inVillage = false;//locateResult.getRight();

        if (inVillage) {
            var b = level.getBiome(pos);
            BlockState replace = b.is(BiomeTags.HAS_VILLAGE_DESERT) ? SANDSTONE_PATH : PATH;
            replaceCobbleWithPath(level, pos, replace);
        }


        if (!villages.isEmpty()) {


            RandomSource rand = level.random;
            //if two signs will spawn
            boolean twoSigns = true;
            BlockPos village1;
            BlockPos village2;
            int dist1;
            int dist2;


            //only 1 sing found/ 1 sign post. always to closest village. posts that are relatively close to a village will always have two.
            //posts in a village will point away
            if (villages.size() == 1 || (0.3 > rand.nextFloat() && villages.get(0).getFirst() > 192)) {
                dist1 = villages.get(0).getFirst();
                village1 = villages.get(0).getSecond();
                dist2 = dist1;
                village2 = village1;
                twoSigns = false;
            } else {
                boolean inv = rand.nextBoolean();
                dist1 = villages.get(inv ? 0 : 1).getFirst();
                village1 = villages.get(inv ? 0 : 1).getSecond();
                dist2 = villages.get(inv ? 1 : 0).getFirst();
                village2 = villages.get(inv ? 1 : 0).getSecond();
            }


            level.setBlockAndUpdate(pos, ModRegistry.SIGN_POST.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof SignPostBlockTile sign) {
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


                if (CommonConfigs.Spawns.DISTANCE_TEXT.get()) {
                    sign.textHolder.setLine(0, getSignText(dist1));
                    if (twoSigns)
                        sign.textHolder.setLine(1, getSignText(dist2));
                }

                float yaw = Mth.wrapDegrees(90 + 360 * MthUtils.averageAngles((180 - sign.yawUp) / 360f, (180 - sign.yawDown) / 360f));
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

                // var biome = ResourceKey.create(ForgeRegistries.Keys.BIOMES, world.getBiome(pos).value().getRegistryName());

                boolean hasFirefly = false; //(BiomeDictionary.hasType(biome, BiomeDictionary.Type.MAGICAL) ||
                //BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) ||
                // BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY) ? 0.2f : 0.01f) > rand.nextFloat();


                //stone
                if (0.3 > rand.nextFloat() && Mth.degreesDifferenceAbs(sign.getPointingYaw(true) + 180, yaw) > 70) {
                    BlockPos stonePos = pos.below().offset(backDir.getNormal());
                    if (rand.nextBoolean()) {
                        level.setBlock(stonePos, STONE_SLAB, 2);
                    } else {
                        level.setBlock(stonePos, STAIR.setValue(StairBlock.FACING, sideDir), 2);
                    }
                    stonePos = stonePos.offset(sideDir.getNormal());
                    level.setBlock(stonePos, STONE, 2);
                    if (0.35 > rand.nextFloat()) {
                        level.setBlock(stonePos.above(), hasFirefly ? JAR : LANTERN_DOWN, 3);
                        hasGroundLantern = true;
                    }
                    stonePos = stonePos.offset(sideDir.getNormal());
                    if (!RoadSignFeature.isNotSolid(level, stonePos.below())) {
                        if (rand.nextBoolean()) {
                            level.setBlock(stonePos, STONE_SLAB, 2);
                        } else {
                            level.setBlock(stonePos, STAIR.setValue(StairBlock.FACING, sideDir.getOpposite()), 2);
                        }
                    }

                }


                if (!hasGroundLantern) {

                    //lanterns
                    pos = pos.above(2);

                    BlockState light = hasFirefly ? JAR : LANTERN;
                    if (rand.nextInt(5) == 1) {
                        light = ModRegistry.CANDLE_HOLDERS.get(null).get().defaultBlockState()
                                .setValue(CandleHolderBlock.LIT, true)
                                .setValue(CandleHolderBlock.FACE, AttachFace.CEILING);
                    }

                    Direction dir = lampDir.get(rand.nextInt(lampDir.size()));

                    boolean doubleSided = 0.25 > rand.nextFloat();
                    if (doubleSided) {
                        dir = dir.getClockWise();
                    }

                    //wall lanterns
                    if (0.32 > rand.nextFloat()) {
                        topState = 0.32 > rand.nextFloat() ? TRAPDOOR : AIR;

                        WallLanternBlock wl = ModRegistry.WALL_LANTERN.get();
                        wl.placeOn(LANTERN_DOWN, pos.below(), dir, level);

                        //double
                        if (doubleSided) {
                            wl.placeOn(LANTERN_DOWN, pos.below(), dir.getOpposite(), level);
                        }

                    } else {
                        boolean isTrapdoor = 0.4 > rand.nextFloat();

                        if (!isTrapdoor) topState = FENCE;

                        //double
                        if (doubleSided) {
                            BlockPos backPos = pos.relative(dir.getOpposite());

                            level.setBlock(backPos, isTrapdoor ? TRAPDOOR : FENCE, 2);

                            if (0.25 > rand.nextFloat()) {
                                topState = isTrapdoor ? SLAB : LOG;
                            }

                            level.setBlock(backPos.below(), light, 3);
                        }

                        pos = pos.relative(dir);
                        BlockState frontState = isTrapdoor ? TRAPDOOR : FENCE;
                        level.setBlock(pos, frontState, 2);

                        level.setBlock(pos.below(), light, 3);
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
            BlockPos belowPos = generatorPos.below(2);
            level.setBlockAndUpdate(belowPos, ModRegistry.NOTICE_BOARD.get().defaultBlockState().setValue(NoticeBoardBlock.HAS_BOOK, true)
                    .setValue(NoticeBoardBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(level.random)));
            if (level.getBlockEntity(belowPos) instanceof NoticeBoardBlockTile board) {
                board.setDisplayedItem(book);
            }
        }
        level.setBlock(generatorPos, topState, 3);
    }

    private static Component getSignText(int d) {
        int s;
        if (d < 100) s = 10;
        else if (d < 2000) s = 100;
        else s = 1000;
        return Component.translatable("message.supplementaries.road_sign", (((d + (s / 2)) / s) * s));
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
