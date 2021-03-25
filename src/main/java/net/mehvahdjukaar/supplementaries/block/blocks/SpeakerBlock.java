package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.SpeakerBlockGui;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SendSpeakerBlockMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import net.minecraft.block.AbstractBlock.Properties;

public class SpeakerBlock extends Block {
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
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SpeakerBlockTile) {
                ((SpeakerBlockTile) tileentity).setCustomName(stack.getHoverName());
            }
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
                        SpeakerBlockTile speaker = (SpeakerBlockTile) tileentity;
                        MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
                        RegistryKey<World> dimension = world.dimension();
                        if (mcserv != null && !speaker.message.equals("")) {
                            // particle
                            world.blockEvent(pos, this, 0, 0);
                            PlayerList players = mcserv.getPlayerList();

                            ITextComponent message = new StringTextComponent(speaker.getName().getString()+": "+speaker.message).withStyle(TextFormatting.ITALIC);

                            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(), ServerConfigs.cached.SPEAKER_RANGE*speaker.volume,
                                    dimension, NetworkHandler.INSTANCE.toVanillaPacket(
                                            new SendSpeakerBlockMessagePacket(message, speaker.narrator),
                                            NetworkDirection.PLAY_TO_CLIENT));
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SpeakerBlockTile) {
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
        super.triggerEvent(state, world, pos, eventID, eventParam);
        Direction facing = state.getValue(FACING);
        world.addParticle(Registry.SPEAKER_SOUND.get(), pos.getX() + 0.5 + facing.getStepX() * 0.725, pos.getY() + 0.5,
                pos.getZ() + 0.5 + facing.getStepZ() * 0.725, (double) world.random.nextInt(24) / 24.0D, 0.0D, 0.0D);
        return true;
    }
}