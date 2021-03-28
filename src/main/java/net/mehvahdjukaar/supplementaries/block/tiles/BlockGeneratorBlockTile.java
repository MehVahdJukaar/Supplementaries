package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.mehvahdjukaar.supplementaries.world.structures.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.world.structures.StructureLocator;
import net.minecraft.block.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockGeneratorBlockTile extends TileEntity implements ITickableTileEntity {
    private boolean firstTick = true;
    public BlockGeneratorBlockTile() {
        super(Registry.BLOCK_GENERATOR_TILE.get());
    }

    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = SixWayBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((prop) -> prop.getKey().getAxis().isHorizontal()).collect(Util.toMap());


    private final BlockState trapdoor = Blocks.SPRUCE_TRAPDOOR.defaultBlockState();
    private final BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
    private final BlockState lanternDown = Blocks.LANTERN.defaultBlockState();
    private final BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
    private final BlockState jar = Registry.FIREFLY_JAR.get().defaultBlockState();
    private final BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
    private final BlockState stoneSlab = Blocks.STONE_SLAB.defaultBlockState();
    private final BlockState stone = Blocks.STONE.defaultBlockState();
    private final BlockState stair = Blocks.STONE_STAIRS.defaultBlockState();

    private double averageAngles(float a, float b){
        a= (float) (a*Math.PI/180);
        b= (float) (b*Math.PI/180);

        return (180/Math.PI)*MathHelper.atan2(MathHelper.sin(a)+MathHelper.sin(b),MathHelper.cos(a)+MathHelper.cos(b));
    }

    //TODO: this has to be the worst code I've written here
    @Override
    public void tick() {
        //if you are reading this I'm sorry...

        if(this.level instanceof ServerWorld) {
            if(!this.firstTick){
                this.level.removeBlock(this.worldPosition,false);
                return;
            }
            this.firstTick = false;
            ServerWorld world = (ServerWorld) this.level;
            BlockPos pos = this.worldPosition.below(2);

            BlockState topState = this.trapdoor;

            Map<Integer, BlockPos> villages = StructureLocator.find(world,pos,Structure.VILLAGE,25,2);


            if(villages.size()>=1) {

                ArrayList<Direction> lampDir = Direction.Plane.HORIZONTAL.stream().collect(Collectors.toCollection(ArrayList::new));

                ArrayList<Integer> v = new ArrayList<>(villages.keySet());

                Random rand = this.level.random;
                //if two signs will spawn
                boolean twoSigns = true;
                BlockPos village1;
                BlockPos village2;
                boolean inv = rand.nextBoolean();
                //only 1 sing found/ 1 sign post. always to closest village. posts that are relatively close to a village will always have two.
                //posts in a village will point away
                if(villages.size()==1 || (0.3>rand.nextFloat() && v.get(0)>192)){
                    village1 = villages.get(v.get(0));
                    village2 = village1;
                    twoSigns = false;
                }
                else{
                    village1 = villages.get(v.get(inv?0:1));
                    village2 = villages.get(v.get(inv?1:0));
                }



                this.level.setBlock(pos, Registry.SIGN_POST.get().defaultBlockState(), 3);
                TileEntity te = this.level.getBlockEntity(pos);
                if (te instanceof SignPostBlockTile) {
                    SignPostBlockTile sign = ((SignPostBlockTile) te);
                    sign.setHeldBlock(Blocks.SPRUCE_FENCE.defaultBlockState());


                    boolean left = rand.nextBoolean();

                    sign.up = true;
                    sign.leftUp = left;
                    sign.pointToward(village1,true);



                    sign.down = twoSigns;
                    sign.leftDown = left;
                    sign.pointToward(village2,false);
                    if(Math.abs(sign.yawUp-sign.yawDown)>90) {
                        sign.leftDown = !sign.leftDown;
                        sign.pointToward(village2,false);
                    }

                    //sign.textHolder.setText(0, new StringTextComponent(""+v.get(0)));
                    //sign.textHolder.setText(1, new StringTextComponent(""+v.get(1)));


                    float yaw = MathHelper.wrapDegrees(90 + (float)this.averageAngles(-sign.yawUp+180, -sign.yawDown+180));
                    Direction backDir = Direction.fromYRot(yaw);

                    float diff = MathHelper.degreesDifference(yaw, backDir.toYRot());

                    Direction sideDir = (diff < 0 ? backDir.getClockWise() : backDir.getCounterClockWise());

                    //lamp spawn chances
                    lampDir.remove(backDir);
                    //lampDir.remove(sideDir);
                    lampDir.add(backDir.getOpposite());
                    lampDir.add(backDir.getOpposite());
                    lampDir.add(backDir.getOpposite());
                    if(Math.abs(diff)>30) {
                        lampDir.add(sideDir.getOpposite());
                        lampDir.remove(sideDir);
                    }

                    boolean hasGroundLantern = false;

                    //stone
                    if(0.3>rand.nextFloat() && MathHelper.degreesDifferenceAbs(sign.getPointingYaw(true)+180,yaw)>70) {
                        BlockPos stonePos = pos.below().offset(backDir.getNormal());
                        if(rand.nextBoolean()) {
                            world.setBlock(stonePos, stoneSlab, 2);
                        }
                        else{
                            world.setBlock(stonePos, stair.setValue(StairsBlock.FACING,sideDir), 2);
                        }
                        stonePos = stonePos.offset(sideDir.getNormal());
                        world.setBlock(stonePos, stone, 2);
                        if(0.35>rand.nextFloat()){
                            world.setBlock(stonePos.above(), lanternDown, 3);
                            hasGroundLantern = true;
                        }
                        stonePos = stonePos.offset(sideDir.getNormal());
                        if(!RoadSignFeature.isNotSolid(world,stonePos.below()) &&
                                RoadSignFeature.isReplaceable(world,stonePos)){
                            if(rand.nextBoolean()) {
                                world.setBlock(stonePos, stoneSlab, 2);
                            }
                            else{
                                world.setBlock(stonePos, stair.setValue(StairsBlock.FACING,sideDir.getOpposite()), 2);
                            }
                        }
                    }


                    if(!hasGroundLantern) {

                        //lanterns
                        pos = pos.above(2);

                        RegistryKey<Biome> biome = RegistryKey.create(ForgeRegistries.Keys.BIOMES, world.getBiome(pos).getRegistryName());
                        float chance = BiomeDictionary.hasType(biome, BiomeDictionary.Type.MAGICAL) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY) ? 0.2f : 0.02f;

                        BlockState light = chance > rand.nextFloat() ? this.jar : this.lantern;


                        Direction dir = lampDir.get(rand.nextInt(lampDir.size()));

                        boolean doubleSided = 0.2 > rand.nextFloat();
                        if (doubleSided) {
                            dir = dir.getClockWise();
                        }

                        boolean isTrapdoor = 0.35 > rand.nextFloat();

                        if (!isTrapdoor) topState = this.fence;

                        //double
                        if (doubleSided) {
                            BlockPos backPos = pos.offset(dir.getOpposite().getNormal());

                            if (isTrapdoor) {
                                world.setBlock(backPos, this.trapdoor, 2);
                                if (0.25 > rand.nextFloat()) {
                                    topState = slab;
                                }
                            }
                            else world.setBlock(backPos, this.fence, 2);

                            world.setBlock(backPos.below(), light, 3);
                        }

                        pos = pos.offset(dir.getNormal());
                        BlockState frontState = isTrapdoor ? this.trapdoor : this.fence;
                        world.setBlock(pos, frontState, 2);

                        world.setBlock(pos.below(), light, 3);
                    }


                    //sign.textHolder.setText(0,new StringTextComponent(yaw+"°"));
                    //sign.textHolder.setText(1,new StringTextComponent(diff+"°"));

                }
            }
            else{
                this.level.setBlock(this.worldPosition.above(),Registry.NOTICE_BOARD.get().defaultBlockState()
                        .setValue(NoticeBoardBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(this.getLevel().random)),3);
            }



            world.setBlock(this.worldPosition, topState,3);


            //this.level.removeBlock(this.worldPosition,false);
        }
    }
}
