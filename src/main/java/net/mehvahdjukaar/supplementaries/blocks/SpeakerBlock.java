package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.gui.SpeakerBlockGui;
import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.SendSpeakerBlockMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
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
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class SpeakerBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public SpeakerBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SpeakerBlockTile) {
                ((SpeakerBlockTile) tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        if (!world.isRemote()) {
            boolean pow = world.isBlockPowered(pos);
            // state changed
            if (pow != state.get(POWERED)) {
                world.setBlockState(pos, state.with(POWERED, pow), 3);
                // can I emit sound?
                Direction facing = state.get(FACING);
                if (pow && world.isAirBlock(pos.offset(facing))) {
                    TileEntity tileentity = world.getTileEntity(pos);
                    if (tileentity instanceof SpeakerBlockTile) {
                        SpeakerBlockTile speaker = (SpeakerBlockTile) tileentity;
                        MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
                        RegistryKey<World> dimension = world.getDimensionKey();
                        if (mcserv != null && !speaker.message.equals("")) {
                            // particle
                            world.addBlockEvent(pos, this, 0, 0);
                            PlayerList players = mcserv.getPlayerList();
                            players.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), ServerConfigs.cached.SPEAKER_RANGE,
                                    dimension, Networking.INSTANCE.toVanillaPacket(
                                            new SendSpeakerBlockMessagePacket(speaker.getName().getString() + ": " + speaker.message,
                                                    speaker.narrator),
                                            NetworkDirection.PLAY_TO_CLIENT));
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof SpeakerBlockTile) {
            // client
            if (world.isRemote) {
                SpeakerBlockGui.open((SpeakerBlockTile) tileentity);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
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
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        Direction facing = state.get(FACING);
        world.addParticle(Registry.SPEAKER_SOUND, pos.getX() + 0.5 + facing.getXOffset() * 0.725, pos.getY() + 0.5,
                pos.getZ() + 0.5 + facing.getZOffset() * 0.725, (double) world.rand.nextInt(24) / 24.0D, 0.0D, 0.0D);
        return true;
    }
}