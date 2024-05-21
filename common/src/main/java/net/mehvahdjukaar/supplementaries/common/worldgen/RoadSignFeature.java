package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlockGeneratorBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class RoadSignFeature extends Feature<RoadSignFeature.Config> {

    public RoadSignFeature(Codec<Config> codec) {
        super(codec);
    }

    public record Config(RandomState randomState, WoodType postWood, WoodType signWood,
                         BlockState fence, BlockState trapdoor, BlockState slab, BlockState log,
                         BlockState cobble, BlockState mossyCobble,
                         BlockState wall, BlockState mossyWall,
                         BlockState lanternUp, BlockState lanternDown,
                         BlockState candleHolder,
                         BlockState stone, BlockState stoneSlab,
                         BlockState stoneStairs, String invalidMessage) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.<Config>create((instance) -> instance.group(
                RandomState.CODEC.fieldOf("random_state").forGetter(Config::randomState),
                WoodType.CODEC.fieldOf("post_wood").forGetter(Config::postWood),
                WoodType.CODEC.fieldOf("sign_wood").forGetter(Config::signWood),
                BlockState.CODEC.fieldOf("cobble").forGetter(Config::cobble),
                BlockState.CODEC.fieldOf("mossy_cobble").forGetter(Config::mossyCobble),
                BlockState.CODEC.fieldOf("wall").forGetter(Config::wall),
                BlockState.CODEC.fieldOf("mossy_wall").forGetter(Config::mossyWall),
                BlockState.CODEC.fieldOf("lantern_up").forGetter(Config::lanternUp),
                BlockState.CODEC.fieldOf("lantern_down").forGetter(Config::lanternDown),
                BlockState.CODEC.fieldOf("candle_holder").forGetter(Config::candleHolder),
                BlockState.CODEC.fieldOf("stone").forGetter(Config::stone),
                BlockState.CODEC.fieldOf("stone_slab").forGetter(Config::stoneSlab),
                BlockState.CODEC.fieldOf("stone_stairs").forGetter(Config::stoneStairs)
        ).apply(instance, Config::of)).comapFlatMap((s) -> {
            if (s.invalidMessage != null)
                return DataResult.error(() -> s.invalidMessage);
            return DataResult.success(s);
        }, Function.identity());

        private static Config of(RandomState randomState, WoodType postWood, WoodType signWood,
                                 BlockState cobble, BlockState mossyCobble,
                                 BlockState wall, BlockState mossyWall,
                                 BlockState lanternUp, BlockState lanternDown,
                                 BlockState candleHolder,
                                 BlockState stone, BlockState stoneSlab,
                                 BlockState stoneStairs) {
            String message = null;
            Block fence = postWood.getBlockOfThis("fence");
            if (fence == null) {
                message = "Post wood type does not have a fence";
                fence = Blocks.AIR;
            }
            Block trapdoor = postWood.getBlockOfThis("trapdoor");
            if (trapdoor == null) {
                message = "Post wood type does not have a trapdoor";
                trapdoor = Blocks.AIR;
            }
            Block slab = postWood.getBlockOfThis("slab");
            if (slab == null) {
                message = "Post wood type does not have a slab";
                slab = Blocks.AIR;
            }
            Block log = postWood.getBlockOfThis("stripped_log");
            if (log == null) {
                message = "Post wood type does not have a valid stripped log";
                log = Blocks.AIR;
            }
            if (!(stoneSlab.getBlock() instanceof SlabBlock)) {
                message = "Stone slab must be a SlabBlock, was " + stoneSlab;
            }
            if (!(stoneStairs.getBlock() instanceof StairBlock)) {
                message = "Stone slab must be a StairBlock, was " + stoneStairs;
            }
            if (!candleHolder.hasProperty(CandleHolderBlock.FACE) || !candleHolder.hasProperty(CandleHolderBlock.LIT)) {
                message = "Candle holder block has to have a face and lit property";
            }
            return new Config(randomState, postWood, signWood, fence.defaultBlockState(), trapdoor.defaultBlockState(),
                    slab.defaultBlockState(), log.defaultBlockState(), cobble, mossyCobble, wall, mossyWall,
                    lanternUp, lanternDown, candleHolder, stone, stoneSlab, stoneStairs, message);
        }
    }

    private record RandomState(float doubleSignChance, float stoneChance, float stoneLanternChance,
                               float candleHolderChance,
                               float wallLanternChance, float doubleLanternChance, float trapdoorChance,
                               float logChance) {
        public static final Codec<RandomState> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.floatRange(0, 1).fieldOf("double_sign_chance").forGetter(RandomState::doubleSignChance),
                Codec.floatRange(0, 1).fieldOf("stone_chance").forGetter(RandomState::stoneChance),
                Codec.floatRange(0, 1).fieldOf("stone_lantern_chance").forGetter(RandomState::stoneLanternChance),
                Codec.floatRange(0, 1).fieldOf("candle_holder_chance").forGetter(RandomState::candleHolderChance),
                Codec.floatRange(0, 1).fieldOf("wall_lantern_chance").forGetter(RandomState::wallLanternChance),
                Codec.floatRange(0, 1).fieldOf("double_lantern_chance").forGetter(RandomState::doubleLanternChance),
                Codec.floatRange(0, 1).fieldOf("trapdoor_chance").forGetter(RandomState::trapdoorChance),
                Codec.floatRange(0, 1).fieldOf("log_chance").forGetter(RandomState::logChance)
        ).apply(instance, RandomState::new));
    }

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState PATH = Blocks.DIRT_PATH.defaultBlockState();
    private static final BlockState SANDSTONE_PATH = Blocks.SMOOTH_SANDSTONE.defaultBlockState();

    public static boolean isNotSolid(LevelAccessor world, BlockPos pos) {
        return !world.isStateAtPosition(pos, (state) -> state.isRedstoneConductor(world, pos));
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {


        WorldGenLevel reader = context.level();
        RandomSource rand = context.random();
        BlockPos pos = context.origin();
        Config c = context.config();

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

        //TODO: fix mossy
        float humidity = SuppPlatformStuff.getDownfall(reader.getBiome(pos).value());


        //generate cobble path
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                reader.setBlock(pos.offset(i, -1, j), c.cobble, 2);

                BlockPos pathPos = pos.offset(i, 0, j);
                double dist = pos.distToCenterSqr(pathPos.getX(), pathPos.getY(), pathPos.getZ()) / 5.2f;

                if (rand.nextFloat() < dist - 0.15) continue;
                boolean m = (humidity * 0.75) > rand.nextFloat();
                reader.setBlock(pathPos, m ? c.mossyCobble : c.cobble, 2);
            }
        }

        //post

        boolean m = (humidity * 0.75) > rand.nextFloat();

        pos = pos.above();
        reader.setBlock(pos, m ? c.mossyWall : c.wall, 2);
        pos = pos.above();
        reader.setBlock(pos, c.fence, 2);
        pos = pos.above();
        reader.setBlock(pos, c.fence, 2);
        reader.setBlock(pos.above(), ModRegistry.BLOCK_GENERATOR.get().defaultBlockState(), 2);
        if (reader.getBlockEntity(pos.above()) instanceof BlockGeneratorBlockTile t) {
            t.setConfig(c);
        }else {
            Supplementaries.LOGGER.error("Failed to get Road Sign Block Entity during generation. How did this happen?");
        }
        return true;
    }


    //post process

    public static void applyPostProcess(Config c, ServerLevel level, BlockPos generatorPos, List<StructureLocator.LocatedStruct> foundVillages) {


        RandomState r = c.randomState;
        BlockState topState = c.trapdoor;

        BlockPos pos = generatorPos.below(2);

        List<Pair<Integer, BlockPos>> villages = new ArrayList<>();
        for (var f : foundVillages) {
            villages.add(Pair.of((int) Mth.sqrt((float) f.pos().distToCenterSqr(pos.getX(), pos.getY(), pos.getZ())), f.pos()));
        }

        //if I am in a village
        boolean inVillage = false;//locateResult.getRight();

        if (inVillage) {
            var b = level.getBiome(pos);
            BlockState replace = b.is(BiomeTags.HAS_VILLAGE_DESERT) ? SANDSTONE_PATH : PATH;
            replaceCobbleWithPath(c, level, pos, replace);
        }


        if (!villages.isEmpty()) {


            RandomSource rand = level.random;
            //if two signs will spawn
            boolean twoSigns = true;
            BlockPos village1;
            BlockPos village2;
            int dist1;
            int dist2;


            //only 1 sing found/ 1 tile post. always to closest village. posts that are relatively close to a village will always have two.
            //posts in a village will point away
            if (villages.size() == 1 || (r.doubleSignChance > rand.nextFloat() && villages.get(0).getFirst() > 192)) {
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
            if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
                tile.setHeldBlock(c.fence);

                boolean left = rand.nextBoolean();


                var up = tile.getSignUp();
                var down = tile.getSignDown();
                up.setActive(true);
                up.setLeft(left);
                up.setWoodType(c.signWood);

                up.pointToward(tile.getBlockPos(), village1);

                down.setActive(twoSigns);
                down.setLeft(left);
                down.setWoodType(c.signWood);

                down.pointToward(tile.getBlockPos(), village2);

                if (Math.abs(up.yaw() - down.yaw()) > 90) {
                    down.toggleDirection();
                    down.pointToward(tile.getBlockPos(), village2);
                }


                if (CommonConfigs.Building.WAY_SIGN_DISTANCE_TEXT.get()) {
                    tile.getTextHolder(0).setMessage(0, getSignText(dist1));
                    if (twoSigns)
                        tile.getTextHolder(1).setMessage(0, getSignText(dist2));
                }

                float yaw = Mth.wrapDegrees(90 + 360 * MthUtils.averageAngles((180 - up.yaw()) / 360f, (180 - down.yaw()) / 360f));
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
                if (rand.nextFloat() < r.stoneChance && Mth.degreesDifferenceAbs(tile.getPointingYaw(true) + 180, yaw) > 70) {
                    BlockPos stonePos = pos.below().offset(backDir.getNormal());
                    if (rand.nextBoolean()) {
                        level.setBlock(stonePos, c.stoneSlab, 2);
                    } else {
                        level.setBlock(stonePos, c.stoneStairs.setValue(StairBlock.FACING, sideDir), 2);
                    }
                    stonePos = stonePos.offset(sideDir.getNormal());
                    level.setBlock(stonePos, c.stone, 2);
                    if (rand.nextFloat() < r.stoneLanternChance) {
                        level.setBlock(stonePos.above(), hasFirefly ? c.lanternDown : c.lanternDown, 3);
                        hasGroundLantern = true;
                    }
                    stonePos = stonePos.offset(sideDir.getNormal());
                    if (!RoadSignFeature.isNotSolid(level, stonePos.below())) {
                        if (rand.nextBoolean()) {
                            level.setBlock(stonePos, c.stoneSlab, 2);
                        } else {
                            level.setBlock(stonePos, c.stoneStairs.setValue(StairBlock.FACING, sideDir.getOpposite()), 2);
                        }
                    }

                }


                if (!hasGroundLantern) {

                    //lanterns
                    pos = pos.above(2);

                    BlockState light = hasFirefly ? c.lanternUp : c.lanternUp;
                    if (rand.nextFloat() < r.candleHolderChance) {
                        light = c.candleHolder
                                .setValue(CandleHolderBlock.LIT, true)
                                .setValue(CandleHolderBlock.FACE, AttachFace.CEILING);
                    }

                    Direction dir = lampDir.get(rand.nextInt(lampDir.size()));

                    boolean doubleSided = r.doubleLanternChance > rand.nextFloat();
                    if (doubleSided) {
                        dir = dir.getClockWise();
                    }

                    //wall lanterns
                    Block wl = CompatObjects.WALL_LANTERN.get();

                    if (wl != null && rand.nextFloat() < r.wallLanternChance) {
                        topState = rand.nextFloat() < r.trapdoorChance ? c.trapdoor : AIR;

                        placeWallLantern(c.lanternDown, level, dir, wl, pos.below());

                        //double
                        if (doubleSided) {
                            placeWallLantern(c.lanternDown, level, dir.getOpposite(), wl, pos.below());
                        }

                    } else {
                        boolean isTrapdoor = r.trapdoorChance > rand.nextFloat();

                        if (!isTrapdoor) topState = c.fence;

                        //double
                        if (doubleSided) {
                            BlockPos backPos = pos.relative(dir.getOpposite());

                            level.setBlock(backPos, isTrapdoor ? c.trapdoor : c.fence, 2);

                            if (r.logChance > rand.nextFloat()) {
                                topState = isTrapdoor ? c.slab : c.log;
                            }

                            level.setBlock(backPos.below(), light, 3);
                        }

                        pos = pos.relative(dir);
                        BlockState frontState = isTrapdoor ? c.trapdoor : c.fence;
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

    private static void replaceCobbleWithPath(Config c, Level world, BlockPos pos, BlockState path) {
        //generate cobble path
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                if (i == 0 && j == 0) continue;
                BlockPos pathPos = pos.offset(i, -2, j);
                BlockState state = world.getBlockState(pathPos);
                if (state.is(c.cobble.getBlock()) || state.is(c.mossyCobble.getBlock())) {
                    world.setBlock(pathPos, path, 2);
                }
            }
        }
    }

    private static void placeWallLantern(BlockState lanternState, ServerLevel level, Direction dir, Block wallLantern, BlockPos pos) {
        pos = pos.relative(dir);
        BlockState state = wallLantern.getStateForPlacement(new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND,
                wallLantern.asItem().getDefaultInstance(), new BlockHitResult(pos.getCenter(), dir, pos, false)));
        if (state != null) level.setBlockAndUpdate(pos, state);
        if (level.getBlockEntity(pos) instanceof IBlockHolder tt) {
            tt.setHeldBlock(lanternState);
        }
    }
}
