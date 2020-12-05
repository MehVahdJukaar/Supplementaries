package net.mehvahdjukaar.supplementaries.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class CogBlock extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    //public static final BooleanProperty RECEIVING_NORTH = BlockStateProperties.NORTH;
    //public static final BooleanProperty RECEIVING_SOUTH = BlockStateProperties.SOUTH;
    //public static final BooleanProperty RECEIVING_EAST = BlockStateProperties.EAST;
    //public static final BooleanProperty RECEIVING_WEST = BlockStateProperties.SOUTH;

    public CogBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(POWER, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote) {
            int pow = world.getRedstonePowerFromNeighbors(pos);
            world.setBlockState(pos, state.with(POWER, MathHelper.clamp(pow, 0, 15)), 1|2|4);
        }
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return Math.max(0,blockState.get(POWER)-1);
    }

}