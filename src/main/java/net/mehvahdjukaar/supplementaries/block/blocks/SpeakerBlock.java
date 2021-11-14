package net.mehvahdjukaar.supplementaries.block.blocks;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.SpeakerBlockGui;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class SpeakerBlock extends Block implements IPeripheralProvider {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SpeakerBlockTile) {
            if (stack.hasCustomHoverName()) {
                ((SpeakerBlockTile) tileentity).setCustomName(stack.getHoverName());
            }
            BlockUtils.addOptionalOwnership(placer, tileentity);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        if (!world.isClientSide()) {
            boolean pow = world.hasNeighborSignal(pos);
            // state changed
            if (pow != state.getValue(POWERED)) {
                world.setBlock(pos, state.setValue(POWERED, pow), 3);
                // can I emit sound?
                Direction facing = state.getValue(FACING);
                if (pow && world.isEmptyBlock(pos.relative(facing))) {
                    TileEntity tileentity = world.getBlockEntity(pos);
                    if (tileentity instanceof SpeakerBlockTile) {
                        ((SpeakerBlockTile) tileentity).sendMessage();
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
                                BlockRayTraceResult hit) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SpeakerBlockTile && ((IOwnerProtected) tileentity).isAccessibleBy(entity)) {
            // client
            if (world.isClientSide) {
                SpeakerBlockGui.open((SpeakerBlockTile) tileentity);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SpeakerBlockTile();
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        if (eventID == 0) {
            Direction facing = state.getValue(FACING);
            world.addParticle(ModRegistry.SPEAKER_SOUND.get(), pos.getX() + 0.5 + facing.getStepX() * 0.725, pos.getY() + 0.5,
                    pos.getZ() + 0.5 + facing.getStepZ() * 0.725, (double) world.random.nextInt(24) / 24.0D, 0.0D, 0.0D);
            return true;
        }
        return super.triggerEvent(state, world, pos, eventID, eventParam);
    }

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull World world, @NotNull BlockPos pos, @NotNull Direction side) {
        TileEntity tile = world.getBlockEntity(pos);
        if(tile instanceof SpeakerBlockTile){
            return ((SpeakerBlockTile) tile).getPeripheral(world, pos, side);
        }
        return LazyOptional.empty();
    }
}