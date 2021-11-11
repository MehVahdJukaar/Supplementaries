package net.mehvahdjukaar.supplementaries.block.blocks;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.SpeakerBlockGui;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.ClientBoundPlaySpeakerMessagePacket;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeakerBlock extends Block implements EntityBlock , IPeripheralProvider {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (worldIn.getBlockEntity(pos) instanceof SpeakerBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            BlockUtils.addOptionalOwnership(placer, tile);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            boolean pow = world.hasNeighborSignal(pos);
            // state changed
            if (pow != state.getValue(POWERED)) {
                world.setBlock(pos, state.setValue(POWERED, pow), 3);
                // can I emit sound?
                Direction facing = state.getValue(FACING);
                if (pow && world.isEmptyBlock(pos.relative(facing))) {
                    if (world.getBlockEntity(pos) instanceof SpeakerBlockTile tile) {
                        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
                        ResourceKey<Level> dimension = world.dimension();
                        if (currentServer != null && !tile.message.equals("")) {
                            // particle
                            world.blockEvent(pos, this, 0, 0);
                            PlayerList players = currentServer.getPlayerList();

                            Component message = new TextComponent(tile.getName().getString() + ": " + tile.message).withStyle(ChatFormatting.ITALIC);

                            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(), ServerConfigs.cached.SPEAKER_RANGE * tile.volume,
                                    dimension, NetworkHandler.INSTANCE.toVanillaPacket(
                                            new ClientBoundPlaySpeakerMessagePacket(message, tile.narrator),
                                            NetworkDirection.PLAY_TO_CLIENT));
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand,
                                 BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SpeakerBlockTile tile && tile.isAccessibleBy(entity)) {
            // client
            if (world.isClientSide) {
                SpeakerBlockGui.open(tile);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SpeakerBlockTile(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
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
    public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        if(world.getBlockEntity(pos) instanceof SpeakerBlockTile tile){
            return tile.getPeripheral(world, pos, side);
        }
        return LazyOptional.empty();
    }

}