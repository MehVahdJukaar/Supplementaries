package net.mehvahdjukaar.supplementaries.integration.quark;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.TinyPotatoBlock;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;

import java.util.Collections;
import java.util.List;

public class TaterInAJarBlock extends TinyPotatoBlock {
    private static final VoxelShape SHAPE = JarBlock.SHAPE;

    public TaterInAJarBlock() {
        super(null);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return ModSounds.JAR;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new Tile(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof Tile tile) {
            tile.interact(player, hand, player.getItemInHand(hand), hitResult.getDirection());
            if (level instanceof ServerLevel serverLevel) {
                AABB box = SHAPE.bounds();
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + box.minX + Math.random() * (box.maxX - box.minX), (double) pos.getY() + box.maxY - 1, (double) pos.getZ() + box.minZ + Math.random() * (box.maxZ - box.minZ), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(this);
        return Collections.singletonList(stack);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            FluidState fluidState = level.getFluidState(pos);
            Item i = ModRegistry.JAR_ITEM.get();
            // i.playReleaseSound( level, ctx.getClickLocation());
            if (!level.isClientSide) {
                Utils.swapItemNBT(player, ctx.getHand(), ctx.getItemInHand(), new ItemStack(i));
            }
            BlockState state = CompatObjects.TATER.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
        }
        return super.getStateForPlacement(ctx);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, QuarkCompat.TATER_IN_A_JAR_TILE.get(), TinyPotatoBlockEntity::commonTick);
    }


    public static class Tile extends TinyPotatoBlockEntity {

        public Tile(BlockPos pos, BlockState state) {
            super(pos, state);
            this.angry = true;
        }

        @Override
        public BlockEntityType<Tile> getType() {
            return QuarkCompat.TATER_IN_A_JAR_TILE.get();
        }
    }
}

