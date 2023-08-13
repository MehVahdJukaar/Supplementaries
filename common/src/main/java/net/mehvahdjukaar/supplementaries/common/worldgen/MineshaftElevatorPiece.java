package net.mehvahdjukaar.supplementaries.common.worldgen;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModWorldgenRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;

public class MineshaftElevatorPiece extends MineshaftPieces.MineShaftPiece {

    private final Direction direction;
    private final byte floor;
    private final boolean hasChain;

    public MineshaftElevatorPiece(StructurePieceSerializationContext context, CompoundTag compoundTag) {
        super(ModWorldgenRegistry.MINESHAFT_ELEVATOR.get(), compoundTag);
        this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
        this.floor = compoundTag.getByte("F");
        this.hasChain = compoundTag.getBoolean("C");
        this.setOrientation(direction);
    }

    public MineshaftElevatorPiece(int depth, BoundingBox boundingBox, @Nullable Direction direction,
                                  byte floor, boolean hasChain, MineshaftStructure.Type type) {
        super(ModWorldgenRegistry.MINESHAFT_ELEVATOR.get(), depth, type, boundingBox);
        this.direction = direction;
        this.floor = floor;
        this.hasChain = hasChain;
        this.setOrientation(direction);
    }

    @Nullable
    public static MineshaftPieces.MineShaftPiece getElevator(
            StructurePieceAccessor pieces, RandomSource random, int x, int y, int z,
            Direction direction, int genDepth, MineshaftStructure.Type type) {
        if (y > 48) {
            return null;
        }
        if (random.nextFloat() < CommonConfigs.Redstone.MINESHAFT_ELEVATOR.get() && CommonConfigs.Redstone.PULLEY_ENABLED.get() && CommonConfigs.Redstone.TURN_TABLE_ENABLED.get()) {
            byte height = 12;

            int floor = random.nextInt(3);
            if (random.nextBoolean() && floor != 2) floor += 1;
            int yOffset = floor * 4;

            BoundingBox boundingBox = switch (direction) {
                default -> new BoundingBox(-1, -yOffset, -4, 3, height - yOffset, 0);
                case SOUTH -> new BoundingBox(-1, -yOffset, 0, 3, height - yOffset, 4);
                case WEST -> new BoundingBox(-4, -yOffset, -1, 0, height - yOffset, 3);
                case EAST -> new BoundingBox(0, -yOffset, -1, 4, height - yOffset, 3);
            };

            boundingBox.move(x, y, z);
            if (pieces.findCollisionPiece(boundingBox) == null) {
                boolean hasChain = random.nextInt(5) == 0;
                return new MineshaftElevatorPiece(genDepth, boundingBox, direction, (byte) floor, hasChain, type);
            }
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("D", this.direction.get2DDataValue());
        tag.putByte("F", this.floor);
        tag.putBoolean("C", this.hasChain);
    }

    @Override
    public void addChildren(StructurePiece piece, StructurePieceAccessor pieces, RandomSource random) {
        int genDepth1 = this.getGenDepth();

        for (int i = 0; i < 3; i++) {
            int y = this.boundingBox.minY() + (i * 4);
            int c = 2 + Mth.abs(i - floor);
            if (random.nextInt(c) != 0) {
                if (i != floor || direction != Direction.SOUTH) {
                    MineshaftPieces.generateAndAddPiece(piece, pieces, random,
                            this.boundingBox.minX() + 1, y, this.boundingBox.minZ() - 1, Direction.NORTH, genDepth1);
                }
            }

            if (random.nextInt(c) != 0) {
                if (i != floor || direction != Direction.EAST) {
                    MineshaftPieces.generateAndAddPiece(piece, pieces, random,
                            this.boundingBox.minX() - 1, y, this.boundingBox.minZ() + 1, Direction.WEST, genDepth1);
                }
            }

            if (random.nextInt(c) != 0) {
                if (i != floor || direction != Direction.WEST) {
                    MineshaftPieces.generateAndAddPiece(piece, pieces, random,
                            this.boundingBox.maxX() + 1, y, this.boundingBox.minZ() + 1, Direction.EAST, genDepth1);
                }
            }

            if (random.nextInt(c) != 0) {
                if (i != floor || direction != Direction.NORTH) {
                    MineshaftPieces.generateAndAddPiece(piece, pieces, random,
                            this.boundingBox.minX() + 1, y, this.boundingBox.maxZ() + 1, Direction.SOUTH, genDepth1);
                }
            }
        }
    }

    //runs once per chunk. can generate in between so random stuff is generated on object creation
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        if (!this.isInInvalidLocation(level, box)) {
            BlockState plank = this.type.getPlanksState();

            int minY = this.boundingBox.minY();
            int minZ = this.boundingBox.minZ();
            int maxZ = this.boundingBox.maxZ();
            int minX = this.boundingBox.minX();
            int maxX = this.boundingBox.maxX();

            for (int f = 0; f < 3; f++) {
                int yInc = f * 4;
                this.generateBox(level, box, minX + 1, minY + yInc, minZ,
                        maxX - 1, minY + 3 - 1 + yInc, maxZ, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(level, box, minX, minY + yInc, minZ + 1,
                        maxX, minY + 3 - 1 + yInc, maxZ - 1, CAVE_AIR, CAVE_AIR, false);

                this.generateBox(level, box, minX + 1, minY + 4 + yInc - 1, minZ + 1,
                        maxX - 1, minY + 4 + yInc - 1, maxZ - 1, CAVE_AIR, CAVE_AIR, false);

                this.maybePlaceCobWeb(level, box, random, 0.06f, minX, minY + yInc + 2, minZ + 1);
                this.maybePlaceCobWeb(level, box, random, 0.06f, minX, minY + yInc + 2, maxZ - 1);

                this.maybePlaceCobWeb(level, box, random, 0.06f, maxX, minY + yInc + 2, minZ + 1);
                this.maybePlaceCobWeb(level, box, random, 0.06f, maxX, minY + yInc + 2, maxZ - 1);


                this.maybePlaceCobWeb(level, box, random, 0.06f, minX + 1, minY + yInc + 2, minZ);
                this.maybePlaceCobWeb(level, box, random, 0.06f, maxX - 1, minY + yInc + 2, minZ);

                this.maybePlaceCobWeb(level, box, random, 0.06f, minX + 1, minY + yInc + 2, maxZ);
                this.maybePlaceCobWeb(level, box, random, 0.06f, maxX - 1, minY + yInc + 2, maxZ);
            }

            this.generateBox(level, box, minX + 1, minY + 4 + 8, minZ + 1,
                    maxX - 1, minY + 4 + 8, maxZ - 1, CAVE_AIR, CAVE_AIR, false);

            int maxY = this.boundingBox.maxY() - 1;

            int i = minY - 1;

            var wood = this.type.getWoodState();

            boolean b1 = fillPillarDownOrChainUp(level, wood, minX, minZ, box);
            boolean b2 = fillPillarDownOrChainUp(level, wood, minX, maxZ, box);
            boolean b3 = fillPillarDownOrChainUp(level, wood, maxX, minZ, box);
            boolean b4 = fillPillarDownOrChainUp(level, wood, maxX, maxZ, box);

            if (!b1 && !b2 && !b3 && !b4) wood = plank;

           this.placeSidePillar(level, box, minX, minY, minZ, maxY - 1, wood);
           this.placeSidePillar(level, box, minX, minY, maxZ, maxY - 1, wood);
           this.placeSidePillar(level, box, maxX, minY, minZ, maxY - 1, wood);
           this.placeSidePillar(level, box, maxX, minY, maxZ, maxY - 1, wood);

            for (int j = minX; j <= maxX; ++j) {
                for (int k = minZ; k <= maxZ; ++k) {
                    this.setPlanksBlock(level, box, plank, j, i, k);
                    if (j == minX || j == maxX || k == minZ || k == maxZ) {
                        this.setPlanksBlock(level, box, plank, j, i + 4, k);
                        this.setPlanksBlock(level, box, plank, j, i + 8, k);
                        placeBlock(level, plank, j, i + 12, k, box);
                    }
                }
            }
            //we need to make this safe across chunk boundaries
            addPulley(level, random, box, minZ, minX, maxY);
        }
    }

    private void maybePlaceCobWeb(WorldGenLevel level, BoundingBox box, RandomSource random, float chance, int x, int y, int z) {
        if (this.isInterior(level, x, y, z, box) && random.nextFloat() < chance
                && this.hasSturdyNeighbours(level, box, x, y, z, 2)) {
            this.placeBlock(level, Blocks.COBWEB.defaultBlockState(), x, y, z, box);
        }
    }

    private void placeSidePillar(WorldGenLevel level, BoundingBox box, int x, int y, int z, int maxY, BlockState state) {
        if (this.isInterior(level, x, y, z, box)) {
            this.generateBox(level, box, x, y, z, x, maxY, z, state, CAVE_AIR, false);
        }
    }

    protected boolean fillPillarDownOrChainUp(WorldGenLevel level, BlockState state, int x, int z, BoundingBox box) {

        int minY = this.boundingBox.minY();
        int maxY = this.boundingBox.maxY() - 1;

        if (!this.isInterior(level, x, minY, z, box)) {
            return false;
        }

        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(x, minY, z);
        int j = 1;
        boolean canKeepGoingDown = this.isReplaceableByStructures(level.getBlockState(new BlockPos(x, minY, z)));
        boolean canKeepGoingUp = this.isReplaceableByStructures(level.getBlockState(new BlockPos(x, maxY, z)));
        if (!canKeepGoingDown) return canKeepGoingUp; //we okhere if we touch either end

        for (; canKeepGoingDown || canKeepGoingUp; ++j) {
            BlockState blockState;
            boolean canBeReplaced;
            if (canKeepGoingDown) {
                mutableBlockPos.setY(minY - j);
                blockState = level.getBlockState(mutableBlockPos);
                canBeReplaced = this.isReplaceableByStructures(blockState) && !blockState.is(Blocks.LAVA);
                if (!canBeReplaced && blockState.isFaceSturdy(level, mutableBlockPos, Direction.UP)) {
                    fillColumnBetween(level, state, mutableBlockPos, minY - j + 1, minY);
                    return true;
                }

                canKeepGoingDown = j <= 20 && canBeReplaced && mutableBlockPos.getY() > level.getMinBuildHeight() + 1;
            }

            if (canKeepGoingUp) {
                mutableBlockPos.setY(maxY + j);
                blockState = level.getBlockState(mutableBlockPos);
                canBeReplaced = this.isReplaceableByStructures(blockState);
                if (!canBeReplaced && this.canHangChainBelow(level, mutableBlockPos, blockState)) {
                    level.setBlock(mutableBlockPos.setY(maxY + 1), this.type.getFenceState(), 2);

                    BlockState chain;
                    if (maxY + 2 > MineshaftElevatorPiece.getRopeCutout() && CommonConfigs.Functional.ROPE_ENABLED.get()) {
                        chain = ModRegistry.ROPE.get().defaultBlockState().setValue(RopeBlock.DISTANCE, 0)
                                .setValue(RopeBlock.UP, true).setValue(RopeBlock.DOWN, true);
                    } else chain = Blocks.CHAIN.defaultBlockState();

                    fillColumnBetween(level, chain, mutableBlockPos, maxY + 2, maxY + j);
                    return false;
                }

                canKeepGoingUp = j <= 50 && canBeReplaced && mutableBlockPos.getY() < level.getMaxBuildHeight() - 1;
            }
        }
        return false;
    }

    //TODSO:FIX pillar gen
    private boolean canHangChainBelow(LevelReader level, BlockPos pos, BlockState state) {
        return Block.canSupportCenter(level, pos, Direction.DOWN) && !(state.getBlock() instanceof FallingBlock);
    }

    private static void fillColumnBetween(WorldGenLevel level, BlockState state, BlockPos.MutableBlockPos pos, int minY, int maxY) {
        for (int i = minY; i < maxY; ++i) {
            level.setBlock(pos.setY(i), state, 2);
        }
    }

    private boolean hasSturdyNeighbours(WorldGenLevel level, BoundingBox box, int x, int y, int z, int required) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(x, y, z);
        int i = 0;

        for (Direction direction : Direction.values()) {
            mutableBlockPos.move(direction);
            if (box.isInside(mutableBlockPos) && level.getBlockState(mutableBlockPos).isFaceSturdy(level, mutableBlockPos, direction.getOpposite())) {
                ++i;
                if (i >= required) {
                    return true;
                }
            }

            mutableBlockPos.move(direction.getOpposite());
        }

        return false;
    }

    public static int getRopeCutout() {
        return 22;
    }

    @Nullable
    public static BlockState getMineshaftRope() {
        Block rope = CommonConfigs.getSelectedRope();
        if (rope == null) return null;
        BlockState ropeState = rope.defaultBlockState();
        if (rope instanceof RopeBlock) {
            ropeState = ropeState.setValue(RopeBlock.UP, true)
                    .setValue(RopeBlock.DISTANCE, 0).setValue(RopeBlock.DOWN, true);
        }
        return ropeState;
    }


    private void addPulley(WorldGenLevel level, RandomSource random, BoundingBox box,
                           int minZ, int minX, int maxY) {

        if(this.getRotation() == null){
            throw new IllegalStateException("Found structure with null rotation. How did this happen?");
        }
        BlockState wood = this.type.getWoodState();
        BlockState plank = this.type.getPlanksState();
        Direction d = direction;
        BlockState ropeBlock = getMineshaftRope();
        boolean hasRope = !hasChain && ropeBlock != null;
        if (!hasRope) ropeBlock = Blocks.CHAIN.defaultBlockState();
        Item ropeItem = ropeBlock.getBlock().asItem();

        BlockPos.MutableBlockPos contraptionPos = new BlockPos.MutableBlockPos(minX + 2, maxY + 1, minZ + 2);

        this.placeBlock(level, ModRegistry.PULLEY_BLOCK.get().defaultBlockState()
                .setValue(PulleyBlock.TYPE, hasRope ? ModBlockProperties.Winding.ROPE : ModBlockProperties.Winding.CHAIN)
                .setValue(PulleyBlock.AXIS, d.getAxis()), contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);

        if (boundingBox.isInside(contraptionPos) && level.getBlockEntity(contraptionPos) instanceof PulleyBlockTile tile) {
            tile.setDisplayedItem(new ItemStack(ropeItem, 16 + random.nextInt(8)));
        }
        contraptionPos.move(d);
        Direction dOpposite = d.getOpposite();
        this.placeBlock(level, ModRegistry.TURN_TABLE.get().defaultBlockState()
                .setValue(TurnTableBlock.INVERTED, d.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                .setValue(TurnTableBlock.FACING, dOpposite), contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d.getClockWise());
        this.placeBlock(level, Blocks.TARGET.defaultBlockState(),
                contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(dOpposite, 2).move(d.getCounterClockWise());
        this.placeBlock(level, wood.setValue(RotatedPillarBlock.AXIS, d.getAxis()),
                contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);

        contraptionPos.move(dOpposite);
        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d.getClockWise());
        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d.getCounterClockWise(), 2);
        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d, 4);


        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d.getClockWise());
        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);
        contraptionPos.move(d.getClockWise());
        this.placeBlock(level, plank, contraptionPos.getX(), contraptionPos.getY(), contraptionPos.getZ(), box);


        this.placeBlock(level, ropeBlock, minX + 2, maxY, minZ + 2, box);
        this.placeBlock(level, ropeBlock, minX + 2, maxY - 1, minZ + 2, box);

        BlockState chest = (hasRope ? ModRegistry.SACK.get() : Blocks.CHEST).defaultBlockState();
        this.placeBlock(level, chest, minX + 2, maxY - 2, minZ + 2, box);
        if(isInterior(level,minX + 2, maxY - 2, minZ + 2, box)){
            //if we placed the tile
            if (level.getBlockEntity(new BlockPos(minX + 2, maxY - 2, minZ + 2)) instanceof RandomizableContainerBlockEntity tile) {
                tile.setLootTable(BuiltInLootTables.ABANDONED_MINESHAFT, random.nextLong());
            }
        }
    }
}
