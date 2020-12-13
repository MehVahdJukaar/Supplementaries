package net.mehvahdjukaar.supplementaries.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class CandleHolderBlock extends SconceWallBlock {

    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.makeCuboidShape(6D, 2.0D, 11D, 10D, 13.0D, 16.0D),
            Direction.SOUTH, Block.makeCuboidShape(6D, 2.0D, 0.0D, 10D, 13.0D, 5D),
            Direction.WEST, Block.makeCuboidShape(11D, 2.0D, 6D, 16.0D, 13.0D, 10D),
            Direction.EAST, Block.makeCuboidShape(0.0D, 2.0D, 6D, 5D, 13.0D, 10D)));

    public CandleHolderBlock(Properties properties, IParticleData particleData) {
        super(properties, particleData);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH).with(LIT, true));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(stateIn.get(LIT)){
            Direction direction = stateIn.get(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.8;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.3125 * (double) direction1.getXOffset(), d1, d2 + 0.3125 * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData, d0 + 0.3125 * (double) direction1.getXOffset(), d1, d2 + 0.3125 * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
        }
    }

}
