package net.mehvahdjukaar.supplementaries.items.crafting;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class TreasureMapRecipe extends CustomRecipe {
    public TreasureMapRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private Level lastWorld = null;

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            if (stack.getItem() == ModRegistry.ANTIQUE_INK.get()) {

                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            }
        }
        boolean match = itemstack != null && itemstack1 != null;
        if (match) {
            lastWorld = worldIn;
        }
        return match;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                ItemStack s = stack.copy();
                s.setCount(1);
                if (lastWorld instanceof ServerLevel level) {
                    renderBiomePreviewMap(level, s);
                    MapItem.lockMap(level, s);

                }
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void renderBiomePreviewMap(ServerLevel pServerLevel, ItemStack pStack) {
        MapItemSavedData mapitemsaveddata = MapItem.getSavedData(pStack, pServerLevel);
        if (mapitemsaveddata != null) {
            if (pServerLevel.dimension() == mapitemsaveddata.dimension) {
                int i = 1 << mapitemsaveddata.scale;
                int j = mapitemsaveddata.x;
                int k = mapitemsaveddata.z;
                Biome[] abiome = new Biome[128 * i * 128 * i];

                for (int l = 0; l < 128 * i; ++l) {
                    for (int i1 = 0; i1 < 128 * i; ++i1) {
                        abiome[l * 128 * i + i1] = pServerLevel.getBiome(new BlockPos((j / i - 64) * i + i1, 0, (k / i - 64) * i + l));
                    }
                }

                for (int l1 = 0; l1 < 128; ++l1) {
                    for (int i2 = 0; i2 < 128; ++i2) {
                        if (l1 > 0 && i2 > 0 && l1 < 127 && i2 < 127) {
                            Biome biome = abiome[l1 * i + i2 * i * 128 * i];
                            int j1 = 8;

                            if (isLand(abiome, i, l1 - 1, i2 - 1)) --j1;
                            if (isLand(abiome, i, l1 - 1, i2 + 1)) --j1;
                            if (isLand(abiome, i, l1 - 1, i2)) --j1;
                            if (isLand(abiome, i, l1 + 1, i2 - 1)) --j1;
                            if (isLand(abiome, i, l1 + 1, i2 + 1)) --j1;
                            if (isLand(abiome, i, l1 + 1, i2)) --j1;
                            if (isLand(abiome, i, l1, i2 - 1)) --j1;
                            if (isLand(abiome, i, l1, i2 + 1)) --j1;


                            int k1 = 3;
                            MaterialColor materialcolor = MaterialColor.NONE;
                            if (mapitemsaveddata.colors[l1 + i2*128]/4 == MaterialColor.WATER.id) { //biome.getDepth() < 0.0F
                                materialcolor = MaterialColor.COLOR_ORANGE;
                                if (j1 > 7 && i2 % 2 == 0) {
                                    k1 = (l1 + (int) (Mth.sin((float) i2 + 0.0F) * 7.0F)) / 8 % 5;
                                    if (k1 == 3) {
                                        k1 = 1;
                                    } else if (k1 == 4) {
                                        k1 = 0;
                                    }
                                } else if (j1 > 7) {
                                    materialcolor = MaterialColor.NONE;
                                } else if (j1 > 5) {
                                    k1 = 1;
                                } else if (j1 > 3) {
                                    k1 = 0;
                                } else if (j1 > 1) {
                                    k1 = 0;
                                }
                            } else if (j1 > 0) {
                                materialcolor = MaterialColor.COLOR_BROWN;
                                if (j1 > 5) {
                                    k1 = 1;
                                } else if (j1 > 3) {
                                    k1 = 0;
                                } else if (j1 > 1) {
                                    k1 = 1;
                                }

                                //if (j1 > 3) {
                                //    k1 = 1;
                                //}
                            }


                            mapitemsaveddata.setColor(l1, i2, (byte) (materialcolor.id * 4 + k1));
                        }
                    }
                }

            }
        }
    }


    public void update(Level pLevel, Entity pViewer, MapItemSavedData pData) {
        if (pLevel.dimension() == pData.dimension && pViewer instanceof Player) {
            int scale = 1 << pData.scale;
            int mapX = pData.x;
            int mapZ = pData.z;
            int playerX = Mth.floor(pViewer.getX() - (double)mapX) / scale + 64;
            int playerZ = Mth.floor(pViewer.getZ() - (double)mapZ) / scale + 64;
            int offset = 128 / scale;
            if (pLevel.dimensionType().hasCeiling()) {
                offset /= 2;
            }

            MapItemSavedData.HoldingPlayer holdingPlayer = pData.getHoldingPlayer((Player)pViewer);
            ++holdingPlayer.step;
            boolean flag = false;

            for(int pX = playerX - offset + 1; pX < playerX + offset; ++pX) {
                if ((pX & 15) == (holdingPlayer.step & 15) || flag) {
                    flag = false;
                    double vertical = 0.0D;

                    for(int pZ = playerZ - offset - 1; pZ < playerZ + offset; ++pZ) {
                        if (pX >= 0 && pZ >= -1 && pX < 128 && pZ < 128) {
                            int i2 = pX - playerX;
                            int j2 = pZ - playerZ;
                            boolean flag1 = i2 * i2 + j2 * j2 > (offset - 2) * (offset - 2);
                            int k2 = (mapX / scale + pX - 64) * scale;
                            int l2 = (mapZ / scale + pZ - 64) * scale;
                            Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                            LevelChunk levelchunk = pLevel.getChunkAt(new BlockPos(k2, 0, l2));
                            if (!levelchunk.isEmpty()) {
                                ChunkPos chunkpos = levelchunk.getPos();
                                int i3 = k2 & 15;
                                int j3 = l2 & 15;
                                int k3 = 0;
                                double d1 = 0.0D;
                                if (pLevel.dimensionType().hasCeiling()) {
                                    int l3 = k2 + l2 * 231871;
                                    l3 = l3 * l3 * 31287121 + l3 * 11;
                                    if ((l3 >> 20 & 1) == 0) {
                                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(pLevel, BlockPos.ZERO), 10);
                                    } else {
                                        multiset.add(Blocks.STONE.defaultBlockState().getMapColor(pLevel, BlockPos.ZERO), 100);
                                    }

                                    d1 = 100.0D;
                                } else {
                                    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                                    BlockPos.MutableBlockPos mutableBlockPos1 = new BlockPos.MutableBlockPos();

                                    for(int i4 = 0; i4 < scale; ++i4) {
                                        for(int j4 = 0; j4 < scale; ++j4) {
                                            int k4 = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                            BlockState blockstate;
                                            if (k4 <= pLevel.getMinBuildHeight() + 1) {
                                                blockstate = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    --k4;
                                                    mutableBlockPos.set(chunkpos.getMinBlockX() + i4 + i3, k4, chunkpos.getMinBlockZ() + j4 + j3);
                                                    blockstate = levelchunk.getBlockState(mutableBlockPos);
                                                } while(blockstate.getMapColor(pLevel, mutableBlockPos) == MaterialColor.NONE && k4 > pLevel.getMinBuildHeight());

                                                if (k4 > pLevel.getMinBuildHeight() && !blockstate.getFluidState().isEmpty()) {
                                                    int l4 = k4 - 1;
                                                    mutableBlockPos1.set(mutableBlockPos);

                                                    BlockState blockstate1;
                                                    do {
                                                        mutableBlockPos1.setY(l4--);
                                                        blockstate1 = levelchunk.getBlockState(mutableBlockPos1);
                                                        ++k3;
                                                    } while(l4 > pLevel.getMinBuildHeight() && !blockstate1.getFluidState().isEmpty());

                                                    blockstate = this.getCorrectStateForFluidBlock(pLevel, blockstate, mutableBlockPos);
                                                }
                                            }

                                            d1 += (double)k4 / (double)(scale * scale);
                                            multiset.add(blockstate.getMapColor(pLevel, mutableBlockPos));
                                        }
                                    }
                                }

                                k3 = k3 / (scale * scale);
                                double d2 = (d1 - vertical) * 4.0D / (double)(scale + 4) + ((double)(pX + pZ & 1) - 0.5D) * 0.4D;
                                int i5 = 1;
                                if (d2 > 0.6D) {
                                    i5 = 2;
                                }

                                if (d2 < -0.6D) {
                                    i5 = 0;
                                }

                                MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
                                if (materialcolor == MaterialColor.WATER) {
                                    d2 = (double)k3 * 0.1D + (double)(pX + pZ & 1) * 0.2D;
                                    i5 = 1;
                                    if (d2 < 0.5D) {
                                        i5 = 2;
                                    }

                                    if (d2 > 0.9D) {
                                        i5 = 0;
                                    }
                                }

                                vertical = d1;
                                if (pZ >= 0 && i2 * i2 + j2 * j2 < offset * offset && (!flag1 || (pX + pZ & 1) != 0)) {
                                    flag |= pData.updateColor(pX, pZ, (byte)(materialcolor.id * 4 + i5));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private static boolean isLand(Biome[] pBiomes, int pScale, int pX, int pZ) {
        return pBiomes[pX * pScale + pZ * pScale * 128 * pScale].getDepth() >= 0.0F;
    }

    private BlockState getCorrectStateForFluidBlock(Level pLevel, BlockState pState, BlockPos pPos) {
        FluidState fluidstate = pState.getFluidState();
        return !fluidstate.isEmpty() && !pState.isFaceSturdy(pLevel, pPos, Direction.UP) ? fluidstate.createLegacyBlock() : pState;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.TREASURE_MAP_RECIPE.get();
    }


}
